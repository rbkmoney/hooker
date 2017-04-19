package com.rbkmoney.hooker.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
