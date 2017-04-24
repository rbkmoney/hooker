package com.rbkmoney.hooker.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.hooker.dao.TaskDao;
import com.rbkmoney.hooker.model.Hook;
import com.rbkmoney.hooker.model.Message;
import com.rbkmoney.hooker.model.MessageJson;
import com.rbkmoney.hooker.service.PostSender;
import com.rbkmoney.hooker.service.crypt.Signer;
import com.rbkmoney.hooker.service.err.PostRequestException;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.http.HttpStatus;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by jeckep on 18.04.17.
 */
@Data
@AllArgsConstructor
public class MessageSender implements Runnable {
    public static Logger log = LoggerFactory.getLogger(MessageSender.class);

    private Hook hook;
    private List<Message> messages;
    private TaskDao taskDao;
    private MessageScheduler workerTaskScheduler;
    private Signer signer;
    private PostSender postSender;

    @Override
    public void run() {
        try {
            workerTaskScheduler.start(hook);

            for (Message message : messages) {
                final String messageJson = MessageJson.buildMessageJson(    message);
                log.debug("Message for send: " + messageJson);
                final String signature = signer.sign(messageJson, hook.getPrivKey());
                int statusCode = postSender.doPost(hook.getUrl(), messageJson, signature);
                if (statusCode != HttpStatus.SC_OK) {
                    log.warn("Wrong status code " + statusCode + " from merchant. Message id = " + message.getId());
                    throw new PostRequestException("Internal server error for message id = " + message.getId());
                }

                log.info("Message: " + message.getId() + " is sent to hook: " + hook.getId());

                taskDao.remove(hook.getId(), message.getId()); //required after message is sent
            }

            workerTaskScheduler.done(hook); // required after all messages processed
        } catch (Exception e) {
            log.warn("Couldn't send message to hook: " + hook.getId());
            workerTaskScheduler.fail(hook); // required if fail to send message
        }
    }
}
