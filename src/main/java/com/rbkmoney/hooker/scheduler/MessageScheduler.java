package com.rbkmoney.hooker.scheduler;

import com.rbkmoney.hooker.model.Message;
import com.rbkmoney.hooker.model.Queue;
import com.rbkmoney.hooker.service.MessageProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import javax.annotation.PostConstruct;
import java.util.stream.IntStream;

@Slf4j
@RequiredArgsConstructor
public class MessageScheduler<M extends Message, Q extends Queue> {
    private final int threadPoolSize;
    private final int delayMillis;
    private final MessageProcessor<M, Q> messageProcessor;
    private final ThreadPoolTaskScheduler executorService;

    @PostConstruct
    public void init() {
        IntStream.range(0, threadPoolSize).forEach(i ->
            executorService.scheduleWithFixedDelay(messageProcessor, delayMillis));
    }
}
