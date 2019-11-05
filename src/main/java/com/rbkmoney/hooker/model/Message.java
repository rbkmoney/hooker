package com.rbkmoney.hooker.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by inalarsanukaev on 20.11.17.
 */
@Getter
@Setter
public class Message {
    private Long id;
    private Long eventId;
    private Long sequenceId;
    private Integer changeId;
    private String eventTime;
    private String partyId;
    private String shopId;
    private EventType eventType;
}
