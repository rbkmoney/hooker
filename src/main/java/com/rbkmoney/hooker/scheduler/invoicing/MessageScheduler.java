package com.rbkmoney.hooker.scheduler.invoicing;

import com.rbkmoney.hooker.dao.MessageDao;
import com.rbkmoney.hooker.dao.impl.InvoicingQueueDao;
import com.rbkmoney.hooker.dao.impl.InvoicingTaskDao;
import com.rbkmoney.hooker.model.Queue;
import com.rbkmoney.hooker.model.Message;
import com.rbkmoney.hooker.model.Task;
import com.rbkmoney.hooker.retry.RetryPoliciesService;
import com.rbkmoney.hooker.retry.RetryPolicyRecord;
import com.rbkmoney.hooker.service.PostSender;
import com.rbkmoney.hooker.service.crypt.Signer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Created by jeckep on 17.04.17.
 */

@Service
public class MessageScheduler {
    Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private InvoicingTaskDao taskDao;

    @Autowired
    private InvoicingQueueDao queueDao;

    @Autowired
    private MessageDao messageDao;

    @Autowired
    private RetryPoliciesService retryPoliciesService;

    @Autowired
    Signer signer;

    @Autowired
    PostSender postSender;

    private final Set<Long> processedQueues = Collections.synchronizedSet(new HashSet<>());
    private ExecutorService executorService;

    public MessageScheduler(@Value("${message.sender.number}") int numberOfWorkers) {
        this.executorService = Executors.newFixedThreadPool(numberOfWorkers);
    }

    @Scheduled(fixedRateString = "${message.scheduler.delay}")
    public void loop() throws InterruptedException {
        final List<Long> currentlyProcessedQueues;
        synchronized (processedQueues) {
            currentlyProcessedQueues = new ArrayList<>(processedQueues);
        }

        final Map<Long, List<Task>> scheduledTasks = getScheduledTasks(currentlyProcessedQueues);
        log.info("scheduledTasks {}", scheduledTasks);
        if (scheduledTasks.entrySet().isEmpty()) {
            return;
        }
        final Map<Long, Queue> healthyQueues = loadQueues(scheduledTasks.keySet())
                .stream().collect(Collectors.toMap(Queue::getId, v -> v));

        log.info("healthyQueues {}", healthyQueues);
        processedQueues.addAll(healthyQueues.keySet());

        final Set<Long> messageIdsToSend = getMessageIdsFilteredByQueues(scheduledTasks, healthyQueues.keySet());
        log.info("messageIdsToSend {}", messageIdsToSend);
        final Map<Long, Message> messagesMap = loadMessages(messageIdsToSend);
        log.info("messagesMap {}", messagesMap);

        for (long queueId : healthyQueues.keySet()) {
            List<Task> tasks = scheduledTasks.get(queueId);
            List<Message> messagesForQueue = new ArrayList<>();
            for (Task task : tasks) {
                Message e = messagesMap.get(task.getMessageId());
                if (e != null) {
                    messagesForQueue.add(e);
                } else {
                    log.error("Message with id {} couldn't be null", task.getMessageId());
                }
            }
            MessageSender messageSender = new MessageSender(healthyQueues.get(queueId), messagesForQueue, taskDao, this, signer, postSender);
            executorService.submit(messageSender);
        }
    }

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

    private List<? extends Queue> loadQueues(Collection<Long> queueIds) {
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

    private Map<Long, Message> loadMessages(Collection<Long> messageIds) {
        List<Message> messages =  messageDao.getBy(messageIds);
        Map<Long, Message> map = new HashMap<>();
        for(Message message: messages){
            map.put(message.getId(), message);
        }
        return map;
    }
}
