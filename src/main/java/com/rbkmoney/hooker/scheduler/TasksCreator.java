package com.rbkmoney.hooker.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Created by jeckep on 12.04.17.
 */

//@Service
public class TasksCreator {
    Logger log = LoggerFactory.getLogger(this.getClass());

//    @Scheduled(fixedDelayString = "${tasks.creator.delay}")
    public void start(){
        log.info("TasksCreator");

        //TODO read events, create tasks, start execute tasks
    }
}
