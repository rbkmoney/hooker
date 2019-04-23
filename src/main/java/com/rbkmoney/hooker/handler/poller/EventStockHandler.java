package com.rbkmoney.hooker.handler.poller;

import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.eventstock.client.EventAction;
import com.rbkmoney.eventstock.client.EventHandler;
import com.rbkmoney.geck.serializer.kit.json.JsonHandler;
import com.rbkmoney.geck.serializer.kit.tbase.TBaseProcessor;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.handler.Handler;
import com.rbkmoney.hooker.utils.HashUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class EventStockHandler implements EventHandler<StockEvent> {

    private static final int INITIAL_VALUE = 3;
    private final AtomicInteger count = new AtomicInteger(INITIAL_VALUE);

    private final List<Handler> pollingEventHandlers;
    private final int divider;
    private final int mod;

    public EventStockHandler(List<Handler> pollingEventHandlers, int divider, int mod) {
        this.pollingEventHandlers = pollingEventHandlers;
        this.divider = divider;
        this.mod = mod;
    }

    public int getDivider() {
        return divider;
    }

    public int getMod() {
        return mod;
    }

    @Override
    public EventAction handle(StockEvent stockEvent, String subsKey) {
        Event processingEvent = stockEvent.getSourceEvent().getProcessingEvent();
        EventPayload payload = processingEvent.getPayload();
        List changes;
        String sourceId;
        if (payload.isSetInvoiceChanges()) {
            changes = payload.getInvoiceChanges();
            sourceId = processingEvent.getSource().getInvoiceId();
        } else if (payload.isSetCustomerChanges()) {
            changes = payload.getCustomerChanges();
            sourceId = processingEvent.getSource().getCustomerId();
        } else return EventAction.CONTINUE;

        if (!HashUtils.checkHashMod(sourceId, divider, mod)) {
            return EventAction.CONTINUE;
        }

        long id = processingEvent.getId();

        for (int i = 0; i < changes.size(); ++i) {
            Object cc = changes.get(i);
            for (Handler pollingEventHandler : pollingEventHandlers) {
                if (pollingEventHandler.accept(cc)) {
                    try {
                        log.info("We got an event {}", new TBaseProcessor().process(stockEvent, JsonHandler.newPrettyJsonInstance()));
                        pollingEventHandler.handle(cc, stockEvent.getId(), stockEvent.getTime(), sourceId, (long) processingEvent.getSequence(), i);
                    } catch (DaoException e) {
                        log.error("DaoException when poller handling with eventId {}", id, e);
                        if (count.decrementAndGet() > 0) {
                            log.warn("Retry handle with eventId {}", id);
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e1) {
                                log.warn("Waiting for retry is interrupted");
                                Thread.currentThread().interrupt();
                            }
                            return EventAction.RETRY;
                        }
                    } catch (Exception e) {
                        log.error("Error when poller handling with id {}",  id, e);
                    }
                    break;
                }
            }
        }
        count.set(INITIAL_VALUE);
        return EventAction.CONTINUE;
    }
}
