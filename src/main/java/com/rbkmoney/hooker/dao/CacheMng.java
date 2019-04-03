package com.rbkmoney.hooker.dao;

import com.rbkmoney.hooker.model.Message;
import com.rbkmoney.hooker.model.Queue;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by inalarsanukaev on 23.11.17.
 */
@Component
@RequiredArgsConstructor
public class CacheMng {
    private static final String MESSAGES_BY_INVOICE = "messagesByInvoice";
    private static final String MESSAGES_BY_IDS = "messagesById";
    private static final String QUEUES = "queues";

    private final CacheManager cacheMng;

    public void putMessage(Message message){
        cacheMng.getCache(MESSAGES_BY_IDS).put(message.getId(), message);
    }

    public void putMessage(String id, Message message){
        cacheMng.getCache(MESSAGES_BY_INVOICE).put(id, message);
    }

    public <T extends Message> T getMessage(String id, Class<T> type) {
        return cacheMng.getCache(MESSAGES_BY_INVOICE).get(id, type);
    }

    public <T extends Message> List<T> getMessages(Collection<Long> ids, Class<T> type) {
        Cache cache = cacheMng.getCache(MESSAGES_BY_IDS);
        return ids.stream().map(id -> cache.get(id, type)).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public  <T extends Queue> List<T> getQueues(Collection<Long> ids, Class<T> type){
        Cache cache = cacheMng.getCache(QUEUES);
        return ids.stream().map(id -> cache.get(id, type)).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public void putQueues(Collection<? extends Queue> queues){
        Cache cache = cacheMng.getCache(QUEUES);
        queues.forEach(q -> cache.put(q.getId(), q));
    }
}
