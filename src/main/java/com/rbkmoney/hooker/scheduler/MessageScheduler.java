package com.rbkmoney.hooker.scheduler;

import com.rbkmoney.hooker.model.Message;
import com.rbkmoney.hooker.model.Queue;
import com.rbkmoney.hooker.service.MessageProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.*;
import java.util.stream.IntStream;

@Slf4j
@RequiredArgsConstructor
public class MessageScheduler<M extends Message, Q extends Queue> {
    private final int threadPoolSize;
    private final int delayMillis;
    private final MessageProcessor<M, Q> messageProcessor;

    private ScheduledExecutorService executorService;

    @PostConstruct
    public void loop() {
        executorService = Executors.newScheduledThreadPool(threadPoolSize);
        IntStream.range(1, threadPoolSize).forEach(i ->
            executorService.scheduleWithFixedDelay(messageProcessor, 0, delayMillis, TimeUnit.SECONDS));
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
