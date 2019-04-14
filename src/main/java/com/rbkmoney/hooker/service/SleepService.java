package com.rbkmoney.hooker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SleepService {

    public void safeSleep(Long timeout) {
        try {
            TimeUnit.MILLISECONDS.sleep(timeout);
        } catch (InterruptedException e) {
            log.error("InterruptedException when waite timeout ", e);
            Thread.currentThread().interrupt();
        }
    }
}
