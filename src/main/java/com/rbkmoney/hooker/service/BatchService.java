package com.rbkmoney.hooker.service;

import com.rbkmoney.hooker.dao.impl.InvoicingMessageDaoImpl;
import com.rbkmoney.hooker.dao.impl.InvoicingQueueDao;
import com.rbkmoney.hooker.dao.impl.InvoicingTaskDao;
import com.rbkmoney.hooker.dao.impl.MessageIdsGeneratorDaoImpl;
import com.rbkmoney.hooker.model.InvoicingMessage;
import com.rbkmoney.hooker.model.InvoicingMessageKey;
import com.rbkmoney.hooker.model.Message;
import com.rbkmoney.hooker.utils.FilterUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
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

    public void process(LinkedHashMap<InvoicingMessageKey, InvoicingMessage> batchMessages){
        log.info("Start processing of batch, size={}", batchMessages.size());
        List<InvoicingMessage> messages = new ArrayList<>(batchMessages.values());
        List<Long> ids = messageIdsGeneratorDao.get(messages.size());
        List<Long> eventIds = messageIdsGeneratorDao.get(messages.size());
        for (int i = 0; i < messages.size(); ++i) {
            messages.get(i).setId(ids.get(i));
            messages.get(i).setEventId(eventIds.get(i));
        }
        List<InvoicingMessage> filteredMessages = invoicingMessageDao.saveBatch(batchMessages);
        log.info("Filtered batch, size={}", filteredMessages.size());
        List<Long> filteredMessageIds = filteredMessages.stream().map(Message::getId).collect(Collectors.toList());
        int[] queueBatchResult = invoicingQueueDao.saveBatchWithPolicies(filteredMessageIds);
        log.info("Queue batch size={}", FilterUtils.filter(queueBatchResult).length);
        int[] taskBatchResult = invoicingTaskDao.saveBatch(filteredMessageIds);
        log.info("Task batch size={}", FilterUtils.filter(taskBatchResult).length);
        log.info("End processing of batch");
    }
}
