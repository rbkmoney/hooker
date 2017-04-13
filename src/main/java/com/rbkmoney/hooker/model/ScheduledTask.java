package com.rbkmoney.hooker.model;

/**
 * Created by jeckep on 13.04.17.
 */
public class ScheduledTask {
    private long eventId;
    private long hookId;

    public ScheduledTask(long eventId, long hookId) {
        this.eventId = eventId;
        this.hookId = hookId;
    }

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public long getHookId() {
        return hookId;
    }

    public void setHookId(long hookId) {
        this.hookId = hookId;
    }
}
