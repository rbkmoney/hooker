package com.rbkmoney.hooker.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.swag_webhook_events.model.Invoice;
import com.rbkmoney.swag_webhook_events.model.InvoiceCartLine;
import com.rbkmoney.swag_webhook_events.model.InvoiceCartLineTaxMode;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InvoiceConverter implements Converter<com.rbkmoney.damsel.domain.Invoice, Invoice> {

    private final ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    public Invoice convert(com.rbkmoney.damsel.domain.Invoice source) {
        return new Invoice()
                .createdAt(OffsetDateTime.parse(source.getCreatedAt(), DateTimeFormatter.ISO_DATE_TIME))
                .status(Invoice.StatusEnum.fromValue(source.getStatus().getSetField().getFieldName()))
                .dueDate(OffsetDateTime.parse(source.getDue(), DateTimeFormatter.ISO_DATE_TIME))
                .amount(source.getCost().getAmount())
                .currency(source.getCost().getCurrency().getSymbolicCode())
                .metadata(source.isSetContext() ? objectMapper.readValue(source.getContext().getData(), HashMap.class) : null)
                .product(source.getDetails().getProduct())
                .description(source.getDetails().getDescription())
                .cart(source.getDetails().isSetCart() ?
                        source.getDetails().getCart().getLines().stream().map(l ->
                                new InvoiceCartLine()
                                        .product(l.getProduct())
                                        .price(l.getPrice().getAmount())
                                        .quantity((long) l.getQuantity())
                                        .cost(l.getPrice().getAmount() * l.getQuantity())
                                        .taxMode(l.getMetadata() != null && l.getMetadata().get("TaxMode") != null ?
                                                new InvoiceCartLineTaxMode()
                                                        .rate(InvoiceCartLineTaxMode.RateEnum.fromValue(l.getMetadata().get("TaxMode").getStr()))
                                                : null))
                                .collect(Collectors.toList())
                        : null);
    }
}
