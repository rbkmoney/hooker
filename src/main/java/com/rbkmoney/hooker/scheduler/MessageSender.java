package com.rbkmoney.hooker.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.hooker.model.Message;
import com.rbkmoney.hooker.model.Queue;
import com.rbkmoney.hooker.service.EventService;
import com.rbkmoney.hooker.service.PostSender;
import com.rbkmoney.hooker.service.crypt.Signer;
import com.rbkmoney.hooker.service.err.PostRequestException;
import com.rbkmoney.swag_webhook_events.model.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

@Slf4j
@RequiredArgsConstructor
public abstract class MessageSender<M extends Message> implements Callable<MessageSender.QueueStatus> {

    private final MessageSender.QueueStatus queueStatus;
    private final List<M> messages;
    private final Signer signer;
    private final PostSender postSender;
    private final EventService<M> eventService;
    private final ObjectMapper objectMapper;

    @Override
    public MessageSender.QueueStatus call() {
        M currentMessage = null;
        try {
            for (M message : messages) {
                currentMessage = message;
                Event event = eventService.getEventByMessage(message);
                final String messageJson = objectMapper.writeValueAsString(event);
                final String signature = signer.sign(messageJson, queueStatus.getQueue().getHook().getPrivKey());
                int statusCode = postSender.doPost(queueStatus.getQueue().getHook().getUrl(), message.getId(), messageJson, signature);
                if (statusCode != HttpStatus.SC_OK) {
                    String wrongCodeMessage = String.format("Wrong status code: %d from merchant, we'll try to resend it. Message with id: %d %s", statusCode, message.getId(), message);
                    log.info(wrongCodeMessage);
                    throw new PostRequestException(wrongCodeMessage);
                }
                queueStatus.messagesDone.add(message.getId());
            }
            queueStatus.setSuccess(true);
        } catch (Exception e) {
            if (currentMessage != null)
                log.warn("Couldn't send message with id {} {} to hook {}. We'll try to resend it", currentMessage.getId(), currentMessage, queueStatus.getQueue().getHook(), e);
            queueStatus.setSuccess(false);
        }
        return queueStatus;
    }

    public static class QueueStatus {
        private Queue queue;
        private boolean isSuccess;
        private List<Long> messagesDone = new ArrayList<>();

        public QueueStatus(Queue queue) {
            this.queue = queue;
        }

        public Queue getQueue() {
            return queue;
        }

        public void setQueue(Queue queue) {
            this.queue = queue;
        }

        public boolean isSuccess() {
            return isSuccess;
        }

        public void setSuccess(boolean success) {
            isSuccess = success;
        }

        public List<Long> getMessagesDone() {
            return messagesDone;
        }
    }
}
