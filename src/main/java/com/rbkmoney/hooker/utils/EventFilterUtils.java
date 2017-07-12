package com.rbkmoney.hooker.utils;

import com.rbkmoney.damsel.webhooker.*;
import com.rbkmoney.hooker.dao.WebhookAdditionalFilter;
import com.rbkmoney.hooker.model.EventType;
import org.apache.thrift.meta_data.StructMetaData;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by inalarsanukaev on 05.04.17.
 */
public class EventFilterUtils {
    public static EventFilter getEventFilter(Collection<WebhookAdditionalFilter> webhookAdditionalFilters) {
        if (webhookAdditionalFilters == null || webhookAdditionalFilters.isEmpty()) {return null;}
        EventFilter eventFilter = new EventFilter();
        InvoiceEventFilter invoiceEventFilter = new InvoiceEventFilter();
        Set<InvoiceEventType> invoiceEventTypes = new HashSet<>();
        invoiceEventFilter.setTypes(invoiceEventTypes);
        eventFilter.setInvoice(invoiceEventFilter);
        for (WebhookAdditionalFilter webhookAdditionalFilter : webhookAdditionalFilters) {
            String shopId = webhookAdditionalFilter.getInvoiceShopId();
            if (shopId != null) {
                eventFilter.getInvoice().setShopId(shopId);
            }
            EventType eventTypeCode = webhookAdditionalFilter.getEventType();
            switch (eventTypeCode) {
                case INVOICE_CREATED:
                    invoiceEventTypes.add(InvoiceEventType.created(new InvoiceCreated()));
                    break;
                case INVOICE_STATUS_CHANGED:
                    InvoiceStatusChanged invoiceStatusChanged = new InvoiceStatusChanged();
                    String status = webhookAdditionalFilter.getInvoiceStatus();
                    if (status != null) {
                        InvoiceStatus value = new InvoiceStatus();
                        InvoiceStatus._Fields fields = InvoiceStatus._Fields.findByName(status);
                        try {
                            Object tBase =  ((StructMetaData) value.getFieldMetaData().get(fields).valueMetaData).structClass.newInstance();
                            value.setFieldValue(fields, tBase);
                            invoiceStatusChanged.setValue(value);
                        } catch (InstantiationException | IllegalAccessException e) {
                            throw new UnsupportedOperationException("Unknown status "+status+"; must be one of these: "+Arrays.toString(InvoiceStatus._Fields.values()));
                        }
                        invoiceStatusChanged.setValue(value);
                    }
                    invoiceEventTypes.add(InvoiceEventType.status_changed(invoiceStatusChanged));
                    break;
                case INVOICE_PAYMENT_STARTED:
                    invoiceEventTypes.add(InvoiceEventType.payment(InvoicePaymentEventType.created(new InvoicePaymentCreated())));
                    break;
                case INVOICE_PAYMENT_STATUS_CHANGED:
                    InvoicePaymentStatusChanged invoicePaymentStatusChanged = new InvoicePaymentStatusChanged();
                    String invoicePaymentStatus = webhookAdditionalFilter.getInvoicePaymentStatus();
                    if (invoicePaymentStatus != null) {
                        InvoicePaymentStatus value1 = new InvoicePaymentStatus();
                        InvoicePaymentStatus._Fields fields = InvoicePaymentStatus._Fields.findByName(invoicePaymentStatus);
                        try {
                            Object tBase = ((StructMetaData) value1.getFieldMetaData().get(fields).valueMetaData).structClass.newInstance();
                            value1.setFieldValue(fields, tBase);
                            invoicePaymentStatusChanged.setValue(value1);
                        } catch (InstantiationException | IllegalAccessException e) {
                            throw new UnsupportedOperationException("Unknown status "+invoicePaymentStatus+"; must be one of these: "+Arrays.toString(InvoicePaymentStatus._Fields.values()));
                        }
                        invoicePaymentStatusChanged.setValue(value1);
                    }
                    invoiceEventTypes.add(InvoiceEventType.payment(InvoicePaymentEventType.status_changed(invoicePaymentStatusChanged)));
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown event code "+eventTypeCode+"; must be one of these: "+Arrays.toString(EventType.values()));
            }
        }
        return eventFilter;
    }

    public static Set<WebhookAdditionalFilter> getWebhookAdditionalFilter(EventFilter eventFilter){
        Set<WebhookAdditionalFilter> eventTypeCodeSet = new HashSet<>();
        if (eventFilter.isSetInvoice()) {
            Set<InvoiceEventType> invoiceEventTypes = eventFilter.getInvoice().getTypes();
            for (InvoiceEventType invoiceEventType : invoiceEventTypes) {
                WebhookAdditionalFilter webhookAdditionalFilter = new WebhookAdditionalFilter();
                eventTypeCodeSet.add(webhookAdditionalFilter);
                if (eventFilter.getInvoice().isSetShopId()) {
                    webhookAdditionalFilter.setInvoiceShopId(eventFilter.getInvoice().getShopId());
                }
                if (invoiceEventType.isSetCreated()) {
                    webhookAdditionalFilter.setEventType(EventType.INVOICE_CREATED);
                } else if (invoiceEventType.isSetStatusChanged()) {
                    webhookAdditionalFilter.setEventType(EventType.INVOICE_STATUS_CHANGED);
                    if (invoiceEventType.getStatusChanged().isSetValue()) {
                        webhookAdditionalFilter.setInvoiceStatus(invoiceEventType.getStatusChanged().getValue().getSetField().getFieldName());
                    }
                } else if (invoiceEventType.isSetPayment()) {
                    if (invoiceEventType.getPayment().isSetCreated()) {
                        webhookAdditionalFilter.setEventType(EventType.INVOICE_PAYMENT_STARTED);
                    } else if (invoiceEventType.getPayment().isSetStatusChanged()) {
                        webhookAdditionalFilter.setEventType(EventType.INVOICE_PAYMENT_STATUS_CHANGED);
                        if (invoiceEventType.getPayment().getStatusChanged().isSetValue()) {
                            webhookAdditionalFilter.setInvoicePaymentStatus(invoiceEventType.getPayment().getStatusChanged().getValue().getSetField().getFieldName());
                        }
                    }
                }
            }
        }
        return eventTypeCodeSet;
    }
}
