package com.rbkmoney.hooker.converter;

import com.rbkmoney.damsel.domain.InvoiceCart;
import com.rbkmoney.damsel.domain.InvoiceLine;
import com.rbkmoney.damsel.msgpack.Value;
import com.rbkmoney.hooker.model.Content;
import com.rbkmoney.swag_webhook_events.model.Invoice;
import com.rbkmoney.swag_webhook_events.model.InvoiceCartLine;
import com.rbkmoney.swag_webhook_events.model.InvoiceCartLineTaxMode;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Component
public class InvoiceConverter implements Converter<com.rbkmoney.damsel.domain.Invoice, Invoice> {

    @Override
    public Invoice convert(com.rbkmoney.damsel.domain.Invoice source) {
        Invoice target = new Invoice();
        target.setCreatedAt(OffsetDateTime.parse(source.getCreatedAt(), DateTimeFormatter.ISO_DATE_TIME));
        target.setStatus(Invoice.StatusEnum.fromValue(source.getStatus().getSetField().getFieldName()));
        target.setDueDate(OffsetDateTime.parse(source.getDue(), DateTimeFormatter.ISO_DATE_TIME));
        target.setAmount(source.getCost().getAmount());
        target.setCurrency(source.getCost().getCurrency().getSymbolicCode());
        Content metadata = new Content();
        metadata.setType(source.getContext().getType());
        metadata.setData(source.getContext().getData());
        target.setMetadata(metadata);
        target.setProduct(source.getDetails().getProduct());
        target.setDescription(source.getDetails().getDescription());
        InvoiceCart cart = source.getDetails().getCart();
        if (cart != null && !cart.getLines().isEmpty()) {
            target.setCart(new ArrayList<>());
            for (InvoiceLine l : cart.getLines()) {
                InvoiceCartLine icp = new InvoiceCartLine();
                icp.setProduct(l.getProduct());
                icp.setPrice(l.getPrice().getAmount());
                icp.setQuantity((long) l.getQuantity());
                icp.setCost(l.getPrice().getAmount() * l.getQuantity());
                if (l.getMetadata() != null) {
                    Value v = l.getMetadata().get("TaxMode");
                    if (v != null) {
                        icp.setTaxMode(new InvoiceCartLineTaxMode().rate(InvoiceCartLineTaxMode.RateEnum.fromValue(v.getStr())));
                    }
                }
                target.getCart().add(icp);
            }
        }
        return target;
    }
}
