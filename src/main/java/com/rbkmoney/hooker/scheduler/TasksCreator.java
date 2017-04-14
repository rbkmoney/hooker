package com.rbkmoney.hooker.scheduler;

import com.rbkmoney.hooker.dao.WebhookDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Created by jeckep on 12.04.17.
 */

@Service
public class TasksCreator {
    Logger log = LoggerFactory.getLogger(this.getClass());


    @Autowired
    WebhookDao webhookDao;

    @Scheduled(fixedDelayString = "${tasks.creator.delay}")
    public void start(){
//        final List<Hook> participatedHooks = webhookDao.getWebhooksBy(eventTypeCodes, partyIds);
        //TODO create tasks, grooup by hooks, start execute tasks
    }
}
