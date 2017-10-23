package com.rbkmoney.hooker.scheduler.invoicing;

import com.rbkmoney.hooker.dao.TaskDao;
import com.rbkmoney.hooker.dao.impl.InvoicingTaskDao;
import com.rbkmoney.hooker.model.Hook;
import com.rbkmoney.hooker.model.Message;
import com.rbkmoney.hooker.model.MessageJson;
import com.rbkmoney.hooker.service.PostSender;
import com.rbkmoney.hooker.service.crypt.Signer;
import com.rbkmoney.hooker.service.err.PostRequestException;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Created by jeckep on 18.04.17.
 */
public class MessageSender implements Runnable {
    public static Logger log = LoggerFactory.getLogger(MessageSender.class);

    private Hook hook;
    private List<Message> messages;
    private TaskDao taskDao;
    private MessageScheduler workerTaskScheduler;
    private Signer signer;
    private PostSender postSender;

    public MessageSender(Hook hook, List<Message> messages, InvoicingTaskDao taskDao, MessageScheduler workerTaskScheduler, Signer signer, PostSender postSender) {
        this.hook = hook;
        this.messages = messages;
        this.taskDao = taskDao;
        this.workerTaskScheduler = workerTaskScheduler;
        this.signer = signer;
        this.postSender = postSender;
    }

    @Override
    public void run() {
        try {
            for (Message message : messages) {
                final String messageJson = MessageJson.buildMessageJson(message);
                final String signature = signer.sign(messageJson, hook.getPrivKey());
                int statusCode = postSender.doPost(hook.getUrl(), messageJson, signature);
                if (statusCode != HttpStatus.SC_OK) {
                    log.warn("Wrong status code {} from merchant, but we don't try to resend it. MessageId {}, invoiceId {}", statusCode, message.getId(), message.getInvoice().getId());
                    //TODO RESTORE IT
                   // throw new PostRequestException("Internal server error for message id = " + message.getId());
                } else {
                    log.info("{} is sent to {}", message, hook);
                }
                taskDao.remove(hook.getId(), message.getId()); //required after message is sent
            }
            workerTaskScheduler.done(hook); // required after all messages processed
        } catch (Exception e) {
            log.warn("Couldn't send message to hook {}. We'll try to resend it", hook.toString(), e);
            workerTaskScheduler.fail(hook); // required if fail to send message
        }
    }
}
