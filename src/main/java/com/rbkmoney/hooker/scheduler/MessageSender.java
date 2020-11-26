package com.rbkmoney.hooker.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.hooker.model.Message;
import com.rbkmoney.hooker.model.Queue;
import com.rbkmoney.hooker.model.QueueStatus;
import com.rbkmoney.hooker.model.Task;
import com.rbkmoney.hooker.service.EventService;
import com.rbkmoney.hooker.service.FaultDetectorService;
import com.rbkmoney.hooker.service.FaultDetectorServiceImpl;
import com.rbkmoney.hooker.service.PostSender;
import com.rbkmoney.hooker.service.crypt.Signer;
import com.rbkmoney.hooker.service.err.PostRequestException;
import com.rbkmoney.swag_webhook_events.model.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class MessageSender<M extends Message, Q extends Queue> {
    private final int connectionPoolSize;
    private final int timeout;
    private final Signer signer;
    private final EventService<M> eventService;
    private final ObjectMapper objectMapper;
    private final FaultDetectorService faultDetector;

    public List<QueueStatus> send(Map<Long, List<Task>> scheduledTasks, Map<Long, Q> queuesMap, Map<Long, M> messagesMap) {
        PostSender postSender = new PostSender(connectionPoolSize, timeout);
        List<QueueStatus> queueStatuses = new ArrayList<>();
        for (Map.Entry<Long, List<Task>> entry : scheduledTasks.entrySet()) {
            Long queueId = entry.getKey();
            List<Task> tasks = entry.getValue();
            Q queue = queuesMap.get(queueId);
            QueueStatus queueStatus = new QueueStatus();
            queueStatus.setQueue(queue);
            M currentMessage = null;
            try {
                for (Task task : tasks) {
                    long messageId = task.getMessageId();
                    M message = messagesMap.get(messageId);
                    currentMessage = message;
                    Event event = eventService.getEventByMessage(message);
                    final String messageJson = objectMapper.writeValueAsString(event);
                    final String signature = signer.sign(messageJson, queue.getHook().getPrivKey());
                    faultDetector.startRequest(queue.getHook().getId(), message.getEventId());
                    int statusCode = postSender.doPost(queue.getHook().getUrl(), message.getId(), messageJson, signature);
                    if (statusCode != HttpStatus.SC_OK) {
                        String wrongCodeMessage = String.format("Wrong status code: %d from merchant, we'll try to resend it. Message with id: %d %s", statusCode, message.getId(), message);
                        log.info(wrongCodeMessage);
                        faultDetector.errorRequest(queue.getHook().getId(), message.getEventId());
                        throw new PostRequestException(wrongCodeMessage);
                    }
                    faultDetector.finishRequest(queue.getHook().getId(), message.getEventId());
                    queueStatus.getMessagesDone().add(message.getId());
                }
                queueStatus.setSuccess(true);
            } catch (Exception e) {
                if (currentMessage != null)
                    log.warn("Couldn't send message with id {} {} to hook {}. We'll try to resend it", currentMessage.getId(), currentMessage, queue.getHook(), e);
                queueStatus.setSuccess(false);
            }
            queueStatuses.add(queueStatus);
        }
        return queueStatuses;
    }
}