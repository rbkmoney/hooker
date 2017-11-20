package com.rbkmoney.hooker.scheduler;

import com.rbkmoney.hooker.dao.MessageDao;
import com.rbkmoney.hooker.dao.QueueDao;
import com.rbkmoney.hooker.dao.TaskDao;
import com.rbkmoney.hooker.model.Message;
import com.rbkmoney.hooker.model.Queue;
import com.rbkmoney.hooker.model.Task;
import com.rbkmoney.hooker.retry.RetryPoliciesService;
import com.rbkmoney.hooker.retry.RetryPolicyRecord;
import com.rbkmoney.hooker.service.PostSender;
import com.rbkmoney.hooker.service.crypt.Signer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Created by jeckep on 17.04.17.
 */

public abstract class MessageScheduler<M extends Message, Q extends Queue> {
    Logger log = LoggerFactory.getLogger(this.getClass());

    private TaskDao taskDao;
    private QueueDao<Q> queueDao;
    private MessageDao<M> messageDao;
    @Autowired
    private RetryPoliciesService retryPoliciesService;
    @Autowired
    private Signer signer;
    @Autowired
    private PostSender postSender;

    private final Set<Long> processedQueues = Collections.synchronizedSet(new HashSet<>());
    private ExecutorService executorService;


    public MessageScheduler(TaskDao taskDao, QueueDao<Q> queueDao, MessageDao<M> messageDao, int numberOfWorkers) {
        this.taskDao = taskDao;
        this.queueDao = queueDao;
        this.messageDao = messageDao;
        this.executorService = Executors.newFixedThreadPool(numberOfWorkers);
    }

    @Scheduled(fixedRateString = "${message.scheduler.delay}")
    public void loop() throws InterruptedException {
        final List<Long> currentlyProcessedQueues;
        synchronized (processedQueues) {
            currentlyProcessedQueues = new ArrayList<>(processedQueues);
        }

        final Map<Long, List<Task>> scheduledTasks = getScheduledTasks(currentlyProcessedQueues);
        if (scheduledTasks.entrySet().isEmpty()) {
            return;
        }
        final Map<Long, Queue> healthyQueues = loadQueues(scheduledTasks.keySet())
                .stream().collect(Collectors.toMap(Queue::getId, v -> v));

        processedQueues.addAll(healthyQueues.keySet());

        final Set<Long> messageIdsToSend = getMessageIdsFilteredByQueues(scheduledTasks, healthyQueues.keySet());
        final Map<Long, M> messagesMap = loadMessages(messageIdsToSend);

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
            MessageSender messageSender = getMessageSender(healthyQueues.get(queueId), messagesForQueue, taskDao, this, signer, postSender);
            executorService.submit(messageSender);
        }
    }

    protected abstract MessageSender getMessageSender(Queue queue, List<M> messagesForQueue, TaskDao taskDao, MessageScheduler messageScheduler, Signer signer, PostSender postSender);

    //worker should invoke this method when it is done with scheduled messages for hookId
    public void done(Queue queue) {
        processedQueues.remove(queue.getId());

        //reset fail count for hook
        if (queue.getRetryPolicyRecord().isFailed()) {
            RetryPolicyRecord record = queue.getRetryPolicyRecord();
            record.reset();
            retryPoliciesService.update(record);
        }
    }

    //worker should invoke this method when it is fail to send message to hookId
    public void fail(Queue queue) {
        processedQueues.remove(queue.getId());

        log.warn("Queue {} failed.", queue.getId());
        retryPoliciesService.getRetryPolicyByType(queue.getHook().getRetryPolicyType())
                .onFail(queue.getRetryPolicyRecord());
    }

    private Map<Long, List<Task>> getScheduledTasks(Collection<Long> excludeQueueIds) {
        return taskDao.getScheduled(excludeQueueIds);
    }

    private List<Queue> loadQueues(Collection<Long> queueIds) {
        List<? extends Queue> queuesWaitingMessages = queueDao.getWithPolicies(queueIds);
        return retryPoliciesService.filter(queuesWaitingMessages);
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
}
