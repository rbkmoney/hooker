package com.rbkmoney.hooker.scheduler;

import com.rbkmoney.hooker.dao.MessageDao;
import com.rbkmoney.hooker.dao.TaskDao;
import com.rbkmoney.hooker.model.EventStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by jeckep on 12.04.17.
 */

@Deprecated
/*
* судя по всему особого смысла в этом сервисе нет, он создает дополнительную задержку в формиовании,
* а всего лишь позволяет избежать одного запроса taskDao.create при каждом сохранении сообщения.
* Это потенциально дать ускорения только при достаточно большом tasks.creator.delay и большой скорости потока ивентов
*
* TODO: просто делать taskDao.create вместе с созданием сообщения, удалить EventStatus так как он тогда не нужен
* */
@Service
public class TasksCreator {
    Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    MessageDao messageDao;

    @Autowired
    TaskDao taskDao;

    @Scheduled(fixedRateString = "${tasks.creator.delay}")
    public void createTasks(){
        List<Long> notScheduledMessageIds = messageDao.getIdsBy(EventStatus.RECEIVED);
        taskDao.create(notScheduledMessageIds);
        messageDao.updateStatus(notScheduledMessageIds, EventStatus.SCHEDULED);
    }
}
