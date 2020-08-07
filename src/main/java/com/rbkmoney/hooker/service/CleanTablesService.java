package com.rbkmoney.hooker.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CleanTablesService {
    @Scheduled(fixedRateString = "${clean.scheduler.delay}")
    public void loop() {

    }
}
