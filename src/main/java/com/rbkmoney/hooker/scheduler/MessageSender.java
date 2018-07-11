package com.rbkmoney.hooker.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rbkmoney.hooker.dao.TaskDao;
import com.rbkmoney.hooker.model.Message;
import com.rbkmoney.hooker.model.Queue;
import com.rbkmoney.hooker.service.PostSender;
import com.rbkmoney.hooker.service.crypt.Signer;
import com.rbkmoney.hooker.service.err.PostRequestException;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by jeckep on 18.04.17.
 */

public abstract class MessageSender<M extends Message> implements Callable<MessageSender.QueueStatus> {
    public static Logger log = LoggerFactory.getLogger(MessageSender.class);

    private MessageSender.QueueStatus queueStatus;
    private List<M> messages;
    private TaskDao taskDao;
    private Signer signer;
    private PostSender postSender;

    public MessageSender(MessageSender.QueueStatus queueStatus, List<M> messages, TaskDao taskDao, Signer signer, PostSender postSender) {
        this.queueStatus = queueStatus;
        this.messages = messages;
        this.taskDao = taskDao;
        this.signer = signer;
        this.postSender = postSender;
    }

    @Override
    public MessageSender.QueueStatus call() throws Exception {
        M currentMessage = null;
        try {
            for (M message : messages) {
                currentMessage = message;
                final String messageJson = getMessageJson(message);
                final String signature = signer.sign(messageJson, queueStatus.getQueue().getHook().getPrivKey());
                int statusCode = postSender.doPost(queueStatus.getQueue().getHook().getUrl(), message.getId(), messageJson, signature);
                if (statusCode != HttpStatus.SC_OK) {
                    String wrongCodeMessage = String.format("Wrong status code: %d from merchant, we'll try to resend it. Message with id: %d %s", statusCode, message.getId(), message);
                    log.info(wrongCodeMessage);
                    throw new PostRequestException(wrongCodeMessage);
                }
                taskDao.remove(queueStatus.getQueue().getId(), message.getId()); //required after message is sent
            }
            queueStatus.setSuccess(true);
        } catch (Exception e) {
            log.warn("Couldn't send message with id {} {} to hook {}. We'll try to resend it", currentMessage.getId(), currentMessage, queueStatus.getQueue().getHook(), e);
            queueStatus.setSuccess(false);
        }
        return queueStatus;
    }

    protected abstract String getMessageJson(M message) throws JsonProcessingException;


    public static class QueueStatus {
        private Queue queue;
        private boolean isSuccess;

        public QueueStatus(Queue queue) {
            this.queue = queue;
        }

        public Queue getQueue() {
            return queue;
        }

        public void setQueue(Queue queue) {
            this.queue = queue;
        }

        public boolean isSuccess() {
            return isSuccess;
        }

        public void setSuccess(boolean success) {
            isSuccess = success;
        }
    }
}
