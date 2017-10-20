package com.rbkmoney.hooker.scheduler.customer;

import com.rbkmoney.hooker.dao.CustomerDao;
import com.rbkmoney.hooker.dao.HookDao;
import com.rbkmoney.hooker.dao.impl.CustomerTaskDao;
import com.rbkmoney.hooker.model.CustomerMessage;
import com.rbkmoney.hooker.model.Hook;
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
public class CustomerMessageScheduler {
    Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CustomerTaskDao taskDao;

    @Autowired
    private HookDao hookDao;

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private RetryPoliciesService retryPoliciesService;

    @Autowired
    Signer signer;

    @Autowired
    PostSender postSender;

    private final Set<Long> processedHooks = Collections.synchronizedSet(new HashSet<>());
    private ExecutorService executorService;

    public CustomerMessageScheduler(@Value("${message.sender.number}") int numberOfWorkers) {
        this.executorService = Executors.newFixedThreadPool(numberOfWorkers);
    }

    @Scheduled(fixedRateString = "${message.scheduler.delay}")
    public void loop() throws InterruptedException {
        final List<Long> currentlyProcessedHooks;
        synchronized (processedHooks) {
            currentlyProcessedHooks = new ArrayList<>(processedHooks);
        }

        final Map<Long, List<Task>> scheduledTasks = getScheduledTasks(currentlyProcessedHooks);
        final Map<Long, Hook> healthyHooks = loadHooks(scheduledTasks.keySet()).stream().collect(Collectors.toMap(v -> v.getId(), v -> v));

        //ready task means - not delayed by failed hook
        int numberOfTasks = numberOfReadyTasks(scheduledTasks, healthyHooks.keySet());
        if(numberOfTasks > 0){
            log.info("Number of not done ready tasks(message->hook): {}", numberOfTasks);
        }

        processedHooks.addAll(healthyHooks.keySet());

        final Set<Long> messageIdsToSend = getMessageIdsFilteredByHooks(scheduledTasks, healthyHooks.keySet());
        final Map<Long, CustomerMessage> messagesMap = loadMessages(messageIdsToSend);

        for (long hookId : healthyHooks.keySet()) {
            List<Task> tasks = scheduledTasks.get(hookId);
            List<CustomerMessage> messagesForHook = new ArrayList<>();
            for (Task task : tasks) {
                CustomerMessage e = messagesMap.get(task.getMessageId());
                if (e != null) {
                    messagesForHook.add(e);
                } else {
                    log.error("Message with id {} couldn't be null", task.getMessageId());
                }
            }
            CustomerMessageSender messageSender = new CustomerMessageSender(healthyHooks.get(hookId), messagesForHook, taskDao, this, signer, postSender);
            executorService.submit(messageSender);
        }
    }

    //worker should invoke this method when it is done with scheduled messages for hookId
    public void done(Hook hook) {
        processedHooks.remove(hook.getId());

        //reset fail count for hook
        if (hook.getRetryPolicyRecord().isFailed()) {
            RetryPolicyRecord record = hook.getRetryPolicyRecord();
            record.reset();
            retryPoliciesService.update(record);
        }
    }

    //worker should invoke this method when it is fail to send message to hookId
    public void fail(Hook hook) {
        processedHooks.remove(hook.getId());

        log.warn("Hook: " + hook.getId() + " failed.");
        retryPoliciesService.getRetryPolicyByType(hook.getRetryPolicyType())
                .onFail(hook.getRetryPolicyRecord());
    }

    private Map<Long, List<Task>> getScheduledTasks(Collection<Long> excludeHooksIds) {
        return taskDao.getScheduled(excludeHooksIds);
    }

    private List<Hook> loadHooks(Collection<Long> hookIds) {
        List<Hook> hooksWaitingMessages = hookDao.getWithPolicies(hookIds);
        return retryPoliciesService.filter(hooksWaitingMessages);
    }

    private Set<Long> getMessageIdsFilteredByHooks(Map<Long, List<Task>> scheduledTasks, Collection<Long> liveHookIds) {
        final Set<Long> messageIds = new HashSet<>();
        for (long hookId : liveHookIds) {
            for (Task t : scheduledTasks.get(hookId)) {
                messageIds.add(t.getMessageId());
            }
        }
        return messageIds;
    }

    private int numberOfReadyTasks(Map<Long, List<Task>> tasks, Collection<Long> liveHookIds){
        int count = 0;
        for(long hookId: liveHookIds){
            count += tasks.get(hookId).size();
        }
        return count;
    }

    private Map<Long, CustomerMessage> loadMessages(Collection<Long> messageIds) {
        List<CustomerMessage> messages =  customerDao.getBy(messageIds);
        Map<Long, CustomerMessage> map = new HashMap<>();
        for(CustomerMessage message: messages){
            map.put(message.getId(), message);
        }
        return map;
    }
}
