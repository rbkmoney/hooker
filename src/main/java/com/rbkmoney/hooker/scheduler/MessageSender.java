package com.rbkmoney.hooker.scheduler;

import com.rbkmoney.hooker.dao.TaskDao;
import com.rbkmoney.hooker.model.Hook;
import com.rbkmoney.hooker.model.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
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

    @Override
    public void run() {
        try{
            for(Message message: messages){
                log.info("Message: " +message.getId() + " is sent to hook: " + hook.getId());
                //TODO send POST message

                taskDao.remove(hook.getId(), message.getId()); //required after message is sent
            }
            workerTaskScheduler.done(hook); // required after all messages processed
        }catch (Exception e){
            workerTaskScheduler.fail(hook); // required if fail to send message
        }
    }
}
