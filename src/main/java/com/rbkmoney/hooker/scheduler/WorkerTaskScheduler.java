package com.rbkmoney.hooker.scheduler;

import com.rbkmoney.hooker.dao.TaskDao;
import com.rbkmoney.hooker.dao.WebhookDao;
import com.rbkmoney.hooker.model.Hook;
import com.rbkmoney.hooker.model.Message;
import com.rbkmoney.hooker.model.Task;
import com.rbkmoney.hooker.retry.RetryPoliciesService;
import com.rbkmoney.hooker.retry.RetryPolicyRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.BlockingQueue;

/**
 * Created by jeckep on 17.04.17.
 */

@Service
//TODO find appropriate name
public class WorkerTaskScheduler {

    @Autowired
    TaskDao taskDao;

    @Autowired
    WebhookDao webhookDao;

    @Autowired
    RetryPoliciesService retryPoliciesService;

    Set<Long> processedHooks = Collections.synchronizedSet(new HashSet<>());

    @Scheduled(fixedRateString = "${tasks.executor.delay}")
    public void loop(){
        final List<Long> currentlyProcessedHooks;

        synchronized (processedHooks){
            currentlyProcessedHooks = new ArrayList<>(processedHooks);
        }

        Map<Long, List<Task>> scheduledTasks = getScheduledTasks(currentlyProcessedHooks);


        final List<Hook> hooksWaitingMessages = webhookDao.getWithPolicies(scheduledTasks.keySet());
        final List<Hook> healthyHooks = retryPoliciesService.filter(hooksWaitingMessages);

        //TODO create worker tasks and add them to blocking queue for workers

    }

    public static class WorkerTask {
        Hook hook;
        List<Message> messages;
    }

    public BlockingQueue<WorkerTask> getTaskQueue(){
        //TODO
        return null;
    }

    //worker should invoke this method when it is done with scheduled messages for hookId
    public void done(Hook hook){
        processedHooks.remove(hook.getId());

        //reset fail count for hook
        if(hook.getRetryPolicyRecord().isFailed()){
            RetryPolicyRecord record = hook.getRetryPolicyRecord();
            record.reset();
            retryPoliciesService.update(record);
        }
    }

    //worker should invoke this method when it is fail to send message to hookId
    public void fail(Hook hook){
        retryPoliciesService.getRetryPolicyByType(hook.getRetryPolicyType())
                .onFail(hook.getRetryPolicyRecord());
        processedHooks.remove(hook.getId());
    }

    private Map<Long, List<Task>> getScheduledTasks(Collection<Long> excludeHooksIds){
        return taskDao.getScheduled(excludeHooksIds);
    }
}
