package com.rbkmoney.hooker.scheduler.customer;

import com.rbkmoney.hooker.dao.TaskDao;
import com.rbkmoney.hooker.dao.impl.CustomerTaskDao;
import com.rbkmoney.hooker.model.CustomerMessage;
import com.rbkmoney.hooker.model.CustomerMessageJson;
import com.rbkmoney.hooker.model.Queue;
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

    private Queue queue;
    private List<CustomerMessage> messages;
    private TaskDao taskDao;
    private CustomerMessageScheduler workerTaskScheduler;
    private Signer signer;
    private PostSender postSender;

    public CustomerMessageSender(Queue queue, List<CustomerMessage> messages, CustomerTaskDao taskDao, CustomerMessageScheduler workerTaskScheduler, Signer signer, PostSender postSender) {
        this.queue = queue;
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
                final String signature = signer.sign(messageJson, queue.getHook().getPrivKey());
                int statusCode = postSender.doPost(queue.getHook().getUrl(), messageJson, signature);
                if (statusCode != HttpStatus.SC_OK) {
                    log.warn("Wrong status code {} from merchant, we try to resend it. MessageId {}, customerId {}", statusCode, message.getId(), message.getCustomer().getId());
                    throw new PostRequestException("Internal server error for message id = " + message.getId());
                }
                log.info("{} is sent to {}", message, queue.getHook());
                taskDao.remove(queue.getId(), message.getId()); //required after message is sent
            }
            workerTaskScheduler.done(queue); // required after all messages processed
        } catch (Exception e) {
            log.warn("Couldn't send message to hook {}. We'll try to resend it", queue.getHook().toString(), e);
            workerTaskScheduler.fail(queue); // required if fail to send message
        }
    }
}
