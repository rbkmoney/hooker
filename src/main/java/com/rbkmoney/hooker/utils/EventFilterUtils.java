package com.rbkmoney.hooker.utils;

import com.rbkmoney.damsel.webhooker.*;
import com.rbkmoney.hooker.dao.WebhookAdditionalFilter;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.swag_webhook_events.model.Event;
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
        if (webhookAdditionalFilters == null || webhookAdditionalFilters.isEmpty()) {
            return null;
        }
        EventFilter eventFilter = new EventFilter();
        EventType firstEventType = webhookAdditionalFilters.iterator().next().getEventType();
        if (firstEventType.isInvoiceEvent()) {
            InvoiceEventFilter invoiceEventFilter = new InvoiceEventFilter();
            Set<InvoiceEventType> invoiceEventTypes = new HashSet<>();
            invoiceEventFilter.setTypes(invoiceEventTypes);
            eventFilter.setInvoice(invoiceEventFilter);
            for (WebhookAdditionalFilter webhookAdditionalFilter : webhookAdditionalFilters) {
                String shopId = webhookAdditionalFilter.getShopId();
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
                                Object tBase = ((StructMetaData) value.getFieldMetaData().get(fields).valueMetaData).structClass.newInstance();
                                value.setFieldValue(fields, tBase);
                                invoiceStatusChanged.setValue(value);
                            } catch (InstantiationException | IllegalAccessException e) {
                                throw new UnsupportedOperationException("Unknown status " + status + "; must be one of these: " + Arrays.toString(InvoiceStatus._Fields.values()));
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
                                throw new UnsupportedOperationException("Unknown status " + invoicePaymentStatus + "; must be one of these: " + Arrays.toString(InvoicePaymentStatus._Fields.values()));
                            }
                            invoicePaymentStatusChanged.setValue(value1);
                        }
                        invoiceEventTypes.add(InvoiceEventType.payment(InvoicePaymentEventType.status_changed(invoicePaymentStatusChanged)));
                        break;
                    case INVOICE_PAYMENT_REFUND_STARTED:
                        invoiceEventTypes.add(InvoiceEventType.payment(InvoicePaymentEventType.invoice_payment_refund_change(InvoicePaymentRefundChange.invoice_payment_refund_created(new InvoicePaymentRefundCreated()))));
                        break;
                    case INVOICE_PAYMENT_REFUND_STATUS_CHANGED:
                        InvoicePaymentRefundStatusChanged invoicePaymentRefundStatusChanged = new InvoicePaymentRefundStatusChanged();
                        String invoicePaymentRefundStatus = webhookAdditionalFilter.getInvoicePaymentRefundStatus();
                        if (invoicePaymentRefundStatus != null) {
                            InvoicePaymentRefundStatus value1 = new InvoicePaymentRefundStatus();
                            InvoicePaymentRefundStatus._Fields fields = InvoicePaymentRefundStatus._Fields.findByName(invoicePaymentRefundStatus);
                            try {
                                Object tBase = ((StructMetaData) value1.getFieldMetaData().get(fields).valueMetaData).structClass.newInstance();
                                value1.setFieldValue(fields, tBase);
                                invoicePaymentRefundStatusChanged.setValue(value1);
                            } catch (InstantiationException | IllegalAccessException e) {
                                throw new UnsupportedOperationException("Unknown status " + invoicePaymentRefundStatus + "; must be one of these: " + Arrays.toString(InvoicePaymentRefundStatus._Fields.values()));
                            }
                            invoicePaymentRefundStatusChanged.setValue(value1);
                        }
                        invoiceEventTypes.add(InvoiceEventType.payment(InvoicePaymentEventType.invoice_payment_refund_change(InvoicePaymentRefundChange.invoice_payment_refund_status_changed(invoicePaymentRefundStatusChanged))));
                        break;
                    default:
                        throw new UnsupportedOperationException("Unknown event code " + eventTypeCode + "; must be one of these: " + Arrays.toString(EventType.values()));
                }
            }
        } else if (firstEventType.isCustomerEvent()) {
            CustomerEventFilter customerEventFilter = new CustomerEventFilter();
            Set<CustomerEventType> customerEventTypes = new HashSet<>();
            customerEventFilter.setTypes(customerEventTypes);
            eventFilter.setCustomer(customerEventFilter);
            for (WebhookAdditionalFilter webhookAdditionalFilter : webhookAdditionalFilters) {
                String shopId = webhookAdditionalFilter.getShopId();
                if (shopId != null) {
                    eventFilter.getCustomer().setShopId(shopId);
                }
                EventType eventTypeCode = webhookAdditionalFilter.getEventType();
                switch (eventTypeCode) {
                    case CUSTOMER_CREATED:
                        customerEventTypes.add(CustomerEventType.created(new CustomerCreated()));
                        break;
                    case CUSTOMER_DELETED:
                        customerEventTypes.add(CustomerEventType.deleted(new CustomerDeleted()));
                        break;
                    case CUSTOMER_READY:
                        customerEventTypes.add(CustomerEventType.ready(new CustomerStatusReady()));
                        break;
                    case CUSTOMER_BINDING_STARTED:
                        customerEventTypes.add(CustomerEventType.binding(CustomerBindingEvent.started(new CustomerBindingStarted())));
                        break;
                    case CUSTOMER_BINDING_SUCCEEDED:
                        customerEventTypes.add(CustomerEventType.binding(CustomerBindingEvent.succeeded(new CustomerBindingSucceeded())));
                        break;
                    case CUSTOMER_BINDING_FAILED:
                        customerEventTypes.add(CustomerEventType.binding(CustomerBindingEvent.failed(new CustomerBindingFailed())));
                        break;
                    default:
                        throw new UnsupportedOperationException("Unknown event code " + eventTypeCode + "; must be one of these: " + Arrays.toString(EventType.values()));
                }
            }
        } else {
            throw new UnsupportedOperationException("Unknown event code " + firstEventType + "; must be one of these: " + Arrays.toString(EventType.values()));
        }
        return eventFilter;
    }

    public static Set<WebhookAdditionalFilter> getWebhookAdditionalFilter(EventFilter eventFilter) {
        Set<WebhookAdditionalFilter> eventTypeCodeSet = new HashSet<>();
        if (eventFilter.isSetInvoice()) {
            Set<InvoiceEventType> invoiceEventTypes = eventFilter.getInvoice().getTypes();
            for (InvoiceEventType invoiceEventType : invoiceEventTypes) {
                WebhookAdditionalFilter webhookAdditionalFilter = new WebhookAdditionalFilter();
                eventTypeCodeSet.add(webhookAdditionalFilter);
                if (eventFilter.getInvoice().isSetShopId()) {
                    webhookAdditionalFilter.setShopId(eventFilter.getInvoice().getShopId());
                }
                if (invoiceEventType.isSetCreated()) {
                    webhookAdditionalFilter.setEventType(EventType.INVOICE_CREATED);
                } else if (invoiceEventType.isSetStatusChanged()) {
                    webhookAdditionalFilter.setEventType(EventType.INVOICE_STATUS_CHANGED);
                    if (invoiceEventType.getStatusChanged().isSetValue()) {
                        webhookAdditionalFilter.setInvoiceStatus(invoiceEventType.getStatusChanged().getValue().getSetField().getFieldName());
                    }
                } else if (invoiceEventType.isSetPayment()) {
                    InvoicePaymentEventType payment = invoiceEventType.getPayment();
                    if (payment.isSetCreated()) {
                        webhookAdditionalFilter.setEventType(EventType.INVOICE_PAYMENT_STARTED);
                    } else if (payment.isSetStatusChanged()) {
                        webhookAdditionalFilter.setEventType(EventType.INVOICE_PAYMENT_STATUS_CHANGED);
                        if (payment.getStatusChanged().isSetValue()) {
                            webhookAdditionalFilter.setInvoicePaymentStatus(payment.getStatusChanged().getValue().getSetField().getFieldName());
                        }
                    } else if (payment.isSetInvoicePaymentRefundChange()) {
                        InvoicePaymentRefundChange refundChange = payment.getInvoicePaymentRefundChange();
                        if (refundChange.isSetInvoicePaymentRefundCreated()) {
                            webhookAdditionalFilter.setEventType(EventType.INVOICE_PAYMENT_REFUND_STARTED);
                        } else if (refundChange.isSetInvoicePaymentRefundStatusChanged()) {
                            webhookAdditionalFilter.setEventType(EventType.INVOICE_PAYMENT_REFUND_STATUS_CHANGED);
                            if (refundChange.getInvoicePaymentRefundStatusChanged().isSetValue()) {
                                webhookAdditionalFilter.setInvoicePaymentRefundStatus(refundChange.getInvoicePaymentRefundStatusChanged().getValue().getSetField().getFieldName());
                            }
                        }
                    }
                }
            }
        } else if (eventFilter.isSetCustomer()) {
            Set<CustomerEventType> customerEventTypes = eventFilter.getCustomer().getTypes();
            for (CustomerEventType customerEventType : customerEventTypes) {
                WebhookAdditionalFilter webhookAdditionalFilter = new WebhookAdditionalFilter();
                eventTypeCodeSet.add(webhookAdditionalFilter);
                if (eventFilter.getCustomer().isSetShopId()) {
                    webhookAdditionalFilter.setShopId(eventFilter.getCustomer().getShopId());
                }
                if (customerEventType.isSetCreated()) {
                    webhookAdditionalFilter.setEventType(EventType.CUSTOMER_CREATED);
                } else if (customerEventType.isSetDeleted()) {
                    webhookAdditionalFilter.setEventType(EventType.CUSTOMER_DELETED);
                } else if (customerEventType.isSetReady()) {
                    webhookAdditionalFilter.setEventType(EventType.CUSTOMER_READY);
                } else if (customerEventType.isSetBinding()) {
                    CustomerBindingEvent binding = customerEventType.getBinding();
                    if (binding.isSetStarted()) {
                        webhookAdditionalFilter.setEventType(EventType.CUSTOMER_BINDING_STARTED);
                    } else if (binding.isSetSucceeded()) {
                        webhookAdditionalFilter.setEventType(EventType.CUSTOMER_BINDING_SUCCEEDED);
                    } else if (binding.isSetFailed()) {
                        webhookAdditionalFilter.setEventType(EventType.CUSTOMER_BINDING_FAILED);
                    }
                }
            }
        }
        return eventTypeCodeSet;
    }

    public static String getTopic(EventFilter eventFilter) {
        if (eventFilter.isSetInvoice()) {
            return Event.TopicEnum.INVOICESTOPIC.getValue();
        }
        if (eventFilter.isSetCustomer()) {
            return Event.TopicEnum.CUSTOMERSTOPIC.getValue();
        }
        throw new UnsupportedOperationException("Unknown topic; must be one of these: " + Arrays.toString(Event.TopicEnum.values()));
    }
}
