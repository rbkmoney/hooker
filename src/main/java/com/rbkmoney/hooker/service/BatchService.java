package com.rbkmoney.hooker.service;

import com.rbkmoney.hooker.dao.impl.InvoicingMessageDaoImpl;
import com.rbkmoney.hooker.dao.impl.InvoicingQueueDao;
import com.rbkmoney.hooker.dao.impl.InvoicingTaskDao;
import com.rbkmoney.hooker.dao.impl.MessageIdsGeneratorDaoImpl;
import com.rbkmoney.hooker.model.InvoicingMessage;
import com.rbkmoney.hooker.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BatchService {

    private final InvoicingMessageDaoImpl invoicingMessageDao;
    private final InvoicingQueueDao invoicingQueueDao;
    private final InvoicingTaskDao invoicingTaskDao;
    private final MessageIdsGeneratorDaoImpl messageIdsGeneratorDao;

    public void process(List<InvoicingMessage> messages) {
        log.info("Start processing of batch, size={}", messages.size());
        List<Long> ids = messageIdsGeneratorDao.get(messages.size());
        List<Long> eventIds = messageIdsGeneratorDao.get(messages.size());
        for (int i = 0; i < messages.size(); ++i) {
            messages.get(i).setId(ids.get(i));
            messages.get(i).setEventId(eventIds.get(i));
        }
        invoicingMessageDao.saveBatch(messages);
        List<Long> messageIds = messages.stream().map(Message::getId).collect(Collectors.toList());
        int[] queueBatchResult = invoicingQueueDao.saveBatchWithPolicies(messageIds);
        log.info("Queue batch size={}", queueBatchResult.length);
        int taskInsertResult = invoicingTaskDao.save(messageIds);
        log.info("Task insert size={}", taskInsertResult);
        log.info("End processing of batch");
    }
}
