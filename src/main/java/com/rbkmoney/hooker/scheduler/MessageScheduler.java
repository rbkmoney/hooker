package com.rbkmoney.hooker.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.hooker.dao.MessageDao;
import com.rbkmoney.hooker.dao.QueueDao;
import com.rbkmoney.hooker.dao.TaskDao;
import com.rbkmoney.hooker.exception.DaoException;
import com.rbkmoney.hooker.model.Message;
import com.rbkmoney.hooker.model.Queue;
import com.rbkmoney.hooker.model.Task;
import com.rbkmoney.hooker.retry.RetryPoliciesService;
import com.rbkmoney.hooker.retry.RetryPolicy;
import com.rbkmoney.hooker.retry.RetryPolicyRecord;
import com.rbkmoney.hooker.service.EventService;
import com.rbkmoney.hooker.service.PostSender;
import com.rbkmoney.hooker.service.crypt.Signer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
public abstract class MessageScheduler<M extends Message, Q extends Queue> {
    @Value("${message.scheduler.threadPoolSize}")
    private int threadPoolSize;
    @Value("${message.scheduler.limit}")
    private int scheduledLimit;
    @Value("${merchant.callback.timeout}")
    private int httpTimeout;
    private TaskDao taskDao;
    private QueueDao<Q> queueDao;
    private MessageDao<M> messageDao;
    private EventService eventService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RetryPoliciesService retryPoliciesService;
    @Autowired
    private Signer signer;
    @Autowired
    private TransactionTemplate transactionTemplate;

    private ExecutorService executorService;

    public MessageScheduler(TaskDao taskDao, QueueDao<Q> queueDao, MessageDao<M> messageDao, EventService eventService, int numberOfWorkers) {
        this.taskDao = taskDao;
        this.queueDao = queueDao;
        this.messageDao = messageDao;
        this.eventService = eventService;
        this.executorService = Executors.newFixedThreadPool(numberOfWorkers);
    }

    @Scheduled(fixedRateString = "${message.scheduler.delay}")
    public void loop() {
        transactionTemplate.execute(k -> {
            processLoop();
            return null;
        });
    }

    private void processLoop() {
        Map<Long, List<Task>> scheduledTasks = taskDao.getScheduled(scheduledLimit);
        log.debug("scheduledTasks {}", scheduledTasks);

        if (scheduledTasks.entrySet().isEmpty()) {
            return;
        }

        Set<Long> queueIds = scheduledTasks.keySet();
        Map<Long, Q> queuesMap = queueDao.getWithPolicies(queueIds).stream().collect(Collectors.toMap(Queue::getId, q -> q));
        Set<Long> messageIds = scheduledTasks.values().stream().flatMap(Collection::stream).map(Task::getMessageId).collect(Collectors.toSet());
        Map<Long, M> messagesMap = messageDao.getBy(messageIds).stream().collect(Collectors.toMap(Message::getId, m -> m));

        List<MessageSender<?>> messageSenders = new ArrayList<>(queueIds.size());
        for (Long queueId : queueIds) {
            List<M> messagesForQueue = scheduledTasks.get(queueId).stream().map(t -> messagesMap.get(t.getMessageId())).collect(Collectors.toList());
            MessageSender messageSender = getMessageSender(new MessageSender.QueueStatus(queuesMap.get(queueId)),
                    messagesForQueue, signer, new PostSender(threadPoolSize, httpTimeout), eventService, objectMapper);
            messageSenders.add(messageSender);
        }

        try {
            List<Future<MessageSender.QueueStatus>> futureList = executorService.invokeAll(messageSenders);
            for (Future<MessageSender.QueueStatus> status : futureList) {
                if (!status.isCancelled()) {
                    try {
                        MessageSender.QueueStatus queueStatus = status.get();
                        try {
                            Queue queue = queueStatus.getQueue();
                            queueStatus.getMessagesDone().forEach(id -> taskDao.remove(queue.getId(), id));
                            if (queueStatus.isSuccess()) {
                                done(queue);
                            } else {
                                fail(queue);
                            }
                        } catch (DaoException e) {
                            log.error("DaoException error when remove sent messages. It's not a big deal, but some messages can be re-sent: {}",
                                    status.get().getMessagesDone());
                        }
                    } catch (ExecutionException e) {
                        log.error("Unexpected error when get queue");
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("Thread was interrupted", e);
            Thread.currentThread().interrupt();
        }
    }

    protected abstract MessageSender getMessageSender(MessageSender.QueueStatus queueStatus, List<M> messagesForQueue,
                                                      Signer signer, PostSender postSender, EventService eventService, ObjectMapper objectMapper);

    private void done(Queue queue) {
        if (queue.getRetryPolicyRecord().isFailed()) {
            RetryPolicyRecord record = queue.getRetryPolicyRecord();
            record.reset();
            retryPoliciesService.update(record);
        }
    }

    private void fail(Queue queue) {
        log.warn("Queue {} failed.", queue.getId());
        RetryPolicy retryPolicy = retryPoliciesService.getRetryPolicyByType(queue.getHook().getRetryPolicyType());
        RetryPolicyRecord retryPolicyRecord = queue.getRetryPolicyRecord();
        retryPolicy.updateFailed(retryPolicyRecord);
        retryPoliciesService.update(retryPolicyRecord);
        if (retryPolicy.shouldDisable(retryPolicyRecord)) {
            queueDao.disable(queue.getId());
            taskDao.removeAll(queue.getId());
            log.warn("Queue {} was disabled according to retry policy.", queue.getId());
        }
    }

    @PreDestroy
    public void preDestroy() {
        executorService.shutdownNow();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn("Failed to stop scheduller in time.");
            } else {
                log.info("Scheduller stopped.");
            }
        } catch (InterruptedException e) {
            log.warn("Waiting for scheduller shutdown is interrupted.");
            Thread.currentThread().interrupt();
        }
    }
}
