package com.rbkmoney.hooker.service;

import com.rbkmoney.hooker.dao.IdsGeneratorDao;
import com.rbkmoney.hooker.dao.impl.InvoicingMessageDaoImpl;
import com.rbkmoney.hooker.dao.impl.InvoicingQueueDao;
import com.rbkmoney.hooker.dao.impl.InvoicingTaskDao;
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
    private final IdsGeneratorDao idsGeneratorDao;

    public void process(List<InvoicingMessage> messages){
        List<Long> ids = idsGeneratorDao.get(messages.size());
        for (int i = 0; i < messages.size(); ++i) {
            messages.get(i).setId(ids.get(i));
        }
        List<InvoicingMessage> filteredMessages = invoicingMessageDao.saveBatch(messages);
        List<Long> filteredMessageIds = filteredMessages.stream().map(Message::getId).collect(Collectors.toList());
        invoicingQueueDao.saveBatchWithPolicies(filteredMessageIds);
        invoicingTaskDao.saveBatch(filteredMessageIds);
    }
}
