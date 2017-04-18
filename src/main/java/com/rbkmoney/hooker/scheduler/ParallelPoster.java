package com.rbkmoney.hooker.scheduler;

import com.rbkmoney.hooker.dao.TaskDao;
import com.rbkmoney.hooker.dao.WebhookDao;
import com.rbkmoney.hooker.model.Hook;
import com.rbkmoney.hooker.model.Message;
import com.rbkmoney.hooker.model.Task;
import com.rbkmoney.hooker.retry.RetryPoliciesService;
import com.rbkmoney.hooker.retry.RetryPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by jeckep on 17.04.17.
 */

@Service
//TODO change name
public class ParallelPoster {

    @Autowired
    TaskDao taskDao;

    @Autowired
    WebhookDao webhookDao;

    @Autowired
    RetryPoliciesService retryPoliciesService;

    Set<Long> processedHooks = Collections.synchronizedSet(new HashSet<>());

    //TODO может мы должны передовать всю необходимую информацию воркерам и полиси и хук
    Map<Long, RetryPolicy> policyMap = Collections.synchronizedMap(new HashMap<Long, RetryPolicy>());

    @Scheduled(fixedRateString = "${tasks.executor.delay}")
    public void loop(){
        final List<Long> currentlyProcessedHooks;

        synchronized (processedHooks){
            currentlyProcessedHooks = new ArrayList<>(processedHooks);
        }

        Map<Long, List<Task>> scheduledTasks = getScheduledTasks(currentlyProcessedHooks);


        final List<Hook> hooksWaitingMessages = webhookDao.getWithPolicies(scheduledTasks.keySet());
        final List<Hook> healthyHooks = retryPoliciesService.filter(hooksWaitingMessages);

        // load related politics - maybe use politics service
        // filter them by politics
    }

    public static class WorkerTask {
        Hook hook;
        Message message;
    }


    //worker should invoke this method when it is done with scheduled messages for hookId
    public void done(long hookId){
        processedHooks.remove(hookId);
    }

    //worker should invoke this method when it is fail to send message to hookId
    public void fail(long hookId){
        RetryPolicy retryPolicy = policyMap.get(hookId);
        retryPolicy.onFail(hookId);
        processedHooks.remove(hookId);
    }

    private Map<Long, List<Task>> getScheduledTasks(Collection<Long> excludeHooksIds){
        return taskDao.getScheduled(excludeHooksIds);
    }

    private List<Task> getActiveHooks(){
        return null;
    }

    private Set<Long> getHooksIds(List<Task> tasks){
        return tasks.stream().map(t -> t.getHookId()).collect(Collectors.toSet());
    }
}
