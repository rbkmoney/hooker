package com.rbkmoney.hooker.service;

import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.hooker.handler.poller.impl.invoicing.AbstractInvoiceEventHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HandlerManager {

    private final List<AbstractInvoiceEventHandler> handlers;

    public Optional<AbstractInvoiceEventHandler> getHandler(InvoiceChange change) {
        return handlers.stream().filter(handler -> handler.accept(change)).findFirst();
    }
}