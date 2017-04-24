package com.rbkmoney.hooker.scheduler;

import com.rbkmoney.hooker.dao.MessageDao;
import com.rbkmoney.hooker.dao.TaskDao;
import com.rbkmoney.hooker.dao.HookDao;
import com.rbkmoney.hooker.model.Hook;
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
//TODO find appropriate name
public class MessageScheduler {
    Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TaskDao taskDao;

    @Autowired
    private HookDao hookDao;

    @Autowired
    private MessageDao messageDao;

    @Autowired
    private RetryPoliciesService retryPoliciesService;

    @Autowired
    Signer signer;

    @Autowired
    PostSender postSender;

    private final Set<Long> processedHooks = Collections.synchronizedSet(new HashSet<>());
    private ExecutorService executorService;

    public MessageScheduler(@Value("${message.sender.number}") int numberOfWorkers) {
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
        final Map<Long, Message> messages = loadMessages(getMessageIds(scheduledTasks, healthyHooks.keySet()))
                .stream().collect(Collectors.toMap(v -> v.getId(), v -> v));


        for (long hookId : scheduledTasks.keySet()) {
            if (healthyHooks.containsKey(hookId)) {
                List<Message> messagesForHook = scheduledTasks.get(hookId)
                        .stream()
                        .map(t -> messages.get(t.getMessageId()))
                        .collect(Collectors.toList());

                MessageSender messageSender = new MessageSender(healthyHooks.get(hookId), messagesForHook, taskDao, this, signer, postSender);
                executorService.submit(messageSender);
            }
        }
    }

    //worker should invoke this method when it starts process
    public void start(Hook hook) {
        processedHooks.add(hook.getId());
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
        log.warn("Hook: " + hook.getId() + " failed.");
        retryPoliciesService.getRetryPolicyByType(hook.getRetryPolicyType())
                .onFail(hook.getRetryPolicyRecord());
        processedHooks.remove(hook.getId());
    }

    private Map<Long, List<Task>> getScheduledTasks(Collection<Long> excludeHooksIds) {
        return taskDao.getScheduled(excludeHooksIds);
    }

    private List<Hook> loadHooks(Collection<Long> hookIds) {
        List<Hook> hooksWaitingMessages = hookDao.getWithPolicies(hookIds);
        return retryPoliciesService.filter(hooksWaitingMessages);
    }

    private Set<Long> getMessageIds(Map<Long, List<Task>> scheduledTasks, Collection<Long> liveHookIds) {
        final Set<Long> messageIds = new HashSet<>();
        for (long hookId : liveHookIds) {
            for (Task t : scheduledTasks.get(hookId)) {
                messageIds.add(t.getMessageId());
            }
        }
        return messageIds;
    }

    private List<Message> loadMessages(Collection<Long> messageIds) {
        return messageDao.getBy(messageIds);
    }
}
