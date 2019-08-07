package com.rbkmoney.hooker.listener;

import com.rbkmoney.machinegun.eventsink.MachineEvent;
import org.springframework.kafka.support.Acknowledgment;

import java.util.List;

public interface MachineEventHandler {

    void handle(List<MachineEvent> messages, Acknowledgment ack);

}
