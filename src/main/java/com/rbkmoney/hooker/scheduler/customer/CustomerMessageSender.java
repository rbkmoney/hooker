package com.rbkmoney.hooker.scheduler.customer;

import com.rbkmoney.hooker.dao.TaskDao;
import com.rbkmoney.hooker.dao.impl.CustomerTaskDao;
import com.rbkmoney.hooker.model.CustomerMessage;
import com.rbkmoney.hooker.model.CustomerMessageJson;
import com.rbkmoney.hooker.model.Hook;
import com.rbkmoney.hooker.service.PostSender;
import com.rbkmoney.hooker.service.crypt.Signer;
import com.rbkmoney.hooker.service.err.PostRequestException;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by jeckep on 18.04.17.
 */
public class CustomerMessageSender implements Runnable {
    public static Logger log = LoggerFactory.getLogger(CustomerMessageSender.class);

    private Hook hook;
    private List<CustomerMessage> messages;
    private TaskDao taskDao;
    private CustomerMessageScheduler workerTaskScheduler;
    private Signer signer;
    private PostSender postSender;

    public CustomerMessageSender(Hook hook, List<CustomerMessage> messages, CustomerTaskDao taskDao, CustomerMessageScheduler workerTaskScheduler, Signer signer, PostSender postSender) {
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
            for (CustomerMessage message : messages) {
                final String messageJson = CustomerMessageJson.buildMessageJson(message);
                final String signature = signer.sign(messageJson, hook.getPrivKey());
                int statusCode = postSender.doPost(hook.getUrl(), messageJson, signature);
                if (statusCode != HttpStatus.SC_OK) {
                    log.warn("Wrong status code " + statusCode + " from merchant. Message id = " + message.getId());
                    throw new PostRequestException("Internal server error for message id = " + message.getId());
                }
                log.info("{} is sent to {}", message, hook);
                taskDao.remove(hook.getId(), message.getId()); //required after message is sent
            }
            workerTaskScheduler.done(hook); // required after all messages processed
        } catch (Exception e) {
            log.warn("Couldn't send message to hook: " + hook.toString(), e);
            workerTaskScheduler.fail(hook); // required if fail to send message
        }
    }
}
