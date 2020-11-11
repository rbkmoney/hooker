package com.rbkmoney.hooker.service;

import com.rbkmoney.hooker.dao.impl.CleanTablesDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CleanTablesService {

    private final CleanTablesDao cleanTablesDao;

    @Value("${clean.scheduler.daysAgo}")
    private int daysAgo;

    @Scheduled(cron = "${clean.scheduler.cron}", zone = "${clean.scheduler.timezone}")
    public void loop() {
        log.info("Start daily cleaning of invoicing queue tables, days ago = {}", daysAgo);
        int affectedRows = cleanTablesDao.cleanInvocing(daysAgo);
        log.info("End daily cleaning of invoicing queue tables, rows deleted = {}", affectedRows);

    }
}
