package com.rbkmoney.hooker.listener;

import com.rbkmoney.machinegun.eventsink.MachineEvent;

public interface MessageListener {

    void handle(MachineEvent message);

}