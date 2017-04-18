package com.rbkmoney.hooker.scheduler;

import com.rbkmoney.hooker.dao.TaskDao;
import com.rbkmoney.hooker.model.Task;
import com.rbkmoney.hooker.retry.RetryPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.Policy;
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

    Set<Long> processedHooks = Collections.synchronizedSet(new HashSet<>());
    Set<Long> failedHooks = Collections.synchronizedSet(new HashSet<>());

    //TODO может мы должны передовать всю необходимую информацию воркерам и полиси и хук
    Map<Long, RetryPolicy> policyMap = Collections.synchronizedMap(new HashMap<Long, RetryPolicy>());

    @Scheduled(fixedRateString = "${tasks.executor.delay}")
    public void loop(){
        Map<Long, List<Task>> scheduledTasks = getScheduledTasks();
        scheduledTasks = filterTasks(scheduledTasks);

        /* мы не можем удалить зафелившийся хук срузу в методе fail(), так как если методы выполнятся в следующем порядке:
        * thread-1: getScheduledTasks()
        * thread-2: retryPolicy.onFail(hookId);
        * thread-2: processedHooks.remove(hookId);
        * thread-1: filterTasks(scheduledTasks)
        * мы получим ситуацию, когда сразу попытаемся послать сообщение в зафейлифщий хук без применения всяких политик переотправки
        * */
        synchronized (failedHooks){
            processedHooks.removeAll(failedHooks);
            failedHooks.clear();
        }

        // read potentialy active tasks
        // load related politics - maybe use politics service
        // filter them by politics
    }


    //worker should invoke this method when it is done with scheduled messages for hookId
    public void done(long hookId){
        processedHooks.remove(hookId);
    }

    //worker should invoke this method when it is fail to send message to hookId
    public void fail(long hookId){
        RetryPolicy retryPolicy = policyMap.get(hookId);
        retryPolicy.onFail(hookId);
        failedHooks.add(hookId);
    }

    private Map<Long, List<Task>> getScheduledTasks(){
        return taskDao.getScheduled();
    }

    private List<Task> getActiveHooks(){
        return null;
    }

    /*
    * по причине того что сообщения должны приходить в хук в их логическом порядке возникновения,
    * мы не можем слать сообщения в один хук в разных тредах. Соответсвенно если сообщениея в хук 1 еще шлются в каком-то треде
    * мы не можем создовать задание для воркеров с новыми сообщениями. Также без этой фильтрации возможна ситуация,
    * когда одно сообщение будет оправлено более одного раза(в одном треде мы еще отправляем сообщение, соответвенно через некоторое
    * время его отправим и удалим таск на его отправку, но паралельно вызвался метод loop и этот таск опять попадет на другой тред)
    * */
    private Map<Long, List<Task>> filterTasks(Map<Long, List<Task>> scheduledTasks){
        synchronized (processedHooks) {
            processedHooks.stream().forEach(hookId -> {
                scheduledTasks.remove(hookId);
            });
        }

        return scheduledTasks;
    }

    private Set<Long> getHooksIds(List<Task> tasks){
        return tasks.stream().map(t -> t.getHookId()).collect(Collectors.toSet());
    }
}
