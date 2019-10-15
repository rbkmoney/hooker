package com.rbkmoney.hooker.scheduler;

import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.dao.MessageDao;
import com.rbkmoney.hooker.dao.QueueDao;
import com.rbkmoney.hooker.dao.TaskDao;
import com.rbkmoney.hooker.model.Message;
import com.rbkmoney.hooker.model.Queue;
import com.rbkmoney.hooker.model.Task;
import com.rbkmoney.hooker.retry.RetryPoliciesService;
import com.rbkmoney.hooker.retry.RetryPolicy;
import com.rbkmoney.hooker.retry.RetryPolicyRecord;
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

    @Value("${message.sender.number}")
    private int connectionPoolSize;
    @Value("${merchant.callback.timeout}")
    private int httpTimeout;

    private TaskDao taskDao;
    private QueueDao<Q> queueDao;
    private MessageDao<M> messageDao;
    @Autowired
    private RetryPoliciesService retryPoliciesService;
    @Autowired
    private Signer signer;
    @Autowired
    private TransactionTemplate transactionTemplate;

    private ExecutorService executorService;


    public MessageScheduler(TaskDao taskDao, QueueDao<Q> queueDao, MessageDao<M> messageDao, int numberOfWorkers) {
        this.taskDao = taskDao;
        this.queueDao = queueDao;
        this.messageDao = messageDao;
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

        final Map<Long, List<Task>> scheduledTasks = getScheduledTasks();

        log.debug("scheduledTasks {}", scheduledTasks);

        if (scheduledTasks.entrySet().isEmpty()) {
            return;
        }
        final Map<Long, Q> healthyQueues = loadQueues(scheduledTasks.keySet())
                .stream().collect(Collectors.toMap(Queue::getId, v -> v));

        log.debug("healthyQueues {}", healthyQueues);

        final Set<Long> messageIdsToSend = getMessageIdsFilteredByQueues(scheduledTasks, healthyQueues.keySet());

        log.info("Schedulled tasks count = {}, after filter = {}", scheduledTasks.size(), messageIdsToSend.size());
        if (messageIdsToSend.isEmpty()) {
            return;
        }

        final Map<Long, M> messagesMap = loadMessages(messageIdsToSend);

        List<MessageSender<?>> messageSenderList = new ArrayList<>(healthyQueues.keySet().size());
        for (long queueId : healthyQueues.keySet()) {
            List<Task> tasks = scheduledTasks.get(queueId);
            List<M> messagesForQueue = new ArrayList<>();
            for (Task task : tasks) {
                M e = messagesMap.get(task.getMessageId());
                if (e != null) {
                    messagesForQueue.add(e);
                } else {
                    log.error("InvoicingMessage with id {} couldn't be null", task.getMessageId());
                }
            }
            MessageSender messageSender = getMessageSender(new MessageSender.QueueStatus(healthyQueues.get(queueId)),
                    messagesForQueue, signer, new PostSender(connectionPoolSize, httpTimeout));
            messageSenderList.add(messageSender);
        }

        try {
            List<Future<MessageSender.QueueStatus>> futureList = executorService.invokeAll(messageSenderList);
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

    protected abstract MessageSender getMessageSender(MessageSender.QueueStatus queueStatus, List<M> messagesForQueue, Signer signer, PostSender postSender);

    private void done(Queue queue) {
        //reset fail count for hook
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

    private Map<Long, List<Task>> getScheduledTasks() {
        return taskDao.getScheduled();
    }

    private List<Q> loadQueues(Collection<Long> queueIds) {
        List<Q> queuesWaitingMessages = queueDao.getWithPolicies(queueIds);
        log.debug("queuesWaitingMessages {}", queuesWaitingMessages.stream().map(Queue::getId).collect(Collectors.toList()));
        return queuesWaitingMessages;
    }

    private Set<Long> getMessageIdsFilteredByQueues(Map<Long, List<Task>> scheduledTasks, Collection<Long> queueIds) {
        final Set<Long> messageIds = new HashSet<>();
        for (long queueId : queueIds) {
            for (Task t : scheduledTasks.get(queueId)) {
                messageIds.add(t.getMessageId());
            }
        }
        return messageIds;
    }

    private Map<Long, M> loadMessages(Collection<Long> messageIds) {
        List<M> messages =  messageDao.getBy(messageIds);
        Map<Long, M> map = new HashMap<>();
        for(M message: messages){
            map.put(message.getId(), message);
        }
        return map;
    }

    @PreDestroy
    public void preDestroy(){
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
