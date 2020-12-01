package com.rbkmoney.hooker.service;

import com.rbkmoney.hooker.dao.HookDao;
import com.rbkmoney.hooker.dao.MessageDao;
import com.rbkmoney.hooker.dao.QueueDao;
import com.rbkmoney.hooker.dao.TaskDao;
import com.rbkmoney.hooker.exception.DaoException;
import com.rbkmoney.hooker.model.*;
import com.rbkmoney.hooker.retry.RetryPoliciesService;
import com.rbkmoney.hooker.retry.RetryPolicy;
import com.rbkmoney.hooker.retry.RetryPolicyRecord;
import com.rbkmoney.hooker.scheduler.MessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class MessageProcessor<M extends Message, Q extends Queue> implements Runnable  {

    private static final double UPDATE_PROBABILITY = 0.25;
    private final HookDao hookDao;
    private final TaskDao taskDao;
    private final QueueDao<Q> queueDao;
    private final MessageDao<M> messageDao;
    private final RetryPoliciesService retryPoliciesService;
    private final TransactionTemplate transactionTemplate;
    private final FaultDetectorService faultDetector;
    private final MessageSender<M, Q> messageSender;

    @Override
    public void run() {
        transactionTemplate.execute(k -> {
            process();
            return null;
        });
    }

    private void process() {
        Map<Long, List<Task>> scheduledTasks = taskDao.getScheduled();
        log.debug("scheduledTasks {}", scheduledTasks);
        if (scheduledTasks.entrySet().isEmpty()) {
            return;
        }

        Set<Long> queueIds = scheduledTasks.keySet();
        Map<Long, Q> queuesMap = queueDao.getWithPolicies(queueIds).stream().collect(Collectors.toMap(Queue::getId, q -> q));
        Set<Long> messageIds = scheduledTasks.values().stream().flatMap(Collection::stream).map(Task::getMessageId).collect(Collectors.toSet());
        Map<Long, M> messagesMap = messageDao.getBy(messageIds).stream().collect(Collectors.toMap(Message::getId, m -> m));

        List<QueueStatus> queueStatuses = messageSender.send(scheduledTasks, queuesMap, messagesMap);
        queueStatuses.forEach(queueStatus -> {
            try {
                Queue queue = queueStatus.getQueue();
                queueStatus.getMessagesDone().forEach(id -> taskDao.remove(queue.getId(), id));
                if (queueStatus.isSuccess()) {
                    done(queue);
                } else {
                    fail(queue);
                }
            } catch (DaoException e) {
                log.error("DaoException error when remove sent messages. It's not a big deal, but some messages may be re-sent: {}",
                        queueStatus.getMessagesDone(), e);
            }
        });
    }

    private void done(Queue queue) {
        if (queue.getRetryPolicyRecord().isFailed()) {
            RetryPolicyRecord record = queue.getRetryPolicyRecord();
            record.reset();
            updatePolicy(record);
            updateAvailability(queue);
        } else {
            if (Math.random() < UPDATE_PROBABILITY) {
                updateAvailability(queue);
            }
        }
    }

    private void fail(Queue queue) {
        log.warn("Queue {} failed.", queue.getId());
        RetryPolicy retryPolicy = retryPoliciesService.getRetryPolicyByType(queue.getHook().getRetryPolicyType());
        RetryPolicyRecord retryPolicyRecord = queue.getRetryPolicyRecord();
        retryPolicy.updateFailed(retryPolicyRecord);
        updatePolicy(retryPolicyRecord);
        updateAvailability(queue);
        if (retryPolicy.shouldDisable(retryPolicyRecord)) {
            queueDao.disable(queue.getId());
            taskDao.removeAll(queue.getId());
            log.warn("Queue {} was disabled according to retry policy.", queue.getId());
        }
    }

    private void updatePolicy(RetryPolicyRecord record) {
        retryPoliciesService.update(record);
        log.info("Queue retry policy has been updated {}", record);
    }

    private void updateAvailability(Queue queue) {
        double rate = faultDetector.getRate(queue.getHook().getId());
        hookDao.updateAvailability(queue.getHook().getId(), rate);
        log.info("Hook {} availability has been updated to {}", queue.getHook().getId(), rate);
    }
}
