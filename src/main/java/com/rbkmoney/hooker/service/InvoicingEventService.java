package com.rbkmoney.hooker.service;

import com.rbkmoney.damsel.payment_processing.Invoice;
import com.rbkmoney.damsel.payment_processing.*;
import com.rbkmoney.hooker.converter.InvoiceConverter;
import com.rbkmoney.hooker.converter.PaymentConverter;
import com.rbkmoney.hooker.converter.RefundConverter;
import com.rbkmoney.hooker.model.InvoicingMessage;
import com.rbkmoney.swag_webhook_events.model.Event;
import com.rbkmoney.swag_webhook_events.model.InvoiceCreated;
import com.rbkmoney.swag_webhook_events.model.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class InvoicingEventService implements EventService<InvoicingMessage> {

    private final UserInfo userInfo = new UserInfo("admin", UserType.internal_user(new InternalUser()));
    private final InvoicingSrv.Iface invoicingClient;
    private final InvoiceConverter invoiceConverter;
    private final PaymentConverter paymentConverter;
    private final RefundConverter refundConverter;

    @SneakyThrows
    @Override
    public Event getByMessage(InvoicingMessage message) {
        Invoice invoiceInfo = invoicingClient.get(userInfo, message.getInvoiceId(), new EventRange().setLimit(message.getSequenceId().intValue()));
        return resolveEvent(message, invoiceInfo)
                .eventID(message.getEventId().intValue())
                .occuredAt(OffsetDateTime.parse(message.getEventTime(), DateTimeFormatter.ISO_DATE_TIME))
                .topic(Event.TopicEnum.INVOICESTOPIC);
    }

    private Event resolveEvent(InvoicingMessage m, Invoice invoiceInfo) {
        switch (m.getEventType()) {
            case INVOICE_CREATED:
                return new InvoiceCreated()
                        .invoice(invoiceConverter.convert(invoiceInfo.getInvoice()));
            case INVOICE_STATUS_CHANGED:
                return resolveInvoiceStatusChanged(m, invoiceInfo);
            case INVOICE_PAYMENT_STARTED:
                return new PaymentStarted()
                        .invoice(invoiceConverter.convert(invoiceInfo.getInvoice()))
                        .payment(paymentConverter.convert(extractPayment(m, invoiceInfo)).fee(m.getPaymentFee()));
            case INVOICE_PAYMENT_STATUS_CHANGED:
                return resolvePaymentStatusChanged(m, invoiceInfo);
            case INVOICE_PAYMENT_REFUND_STARTED:
                return new RefundCreated()
                        .invoice(invoiceConverter.convert(invoiceInfo.getInvoice()))
                        .payment(paymentConverter.convert(extractPayment(m, invoiceInfo)).fee(m.getPaymentFee()))
                        .refund(refundConverter.convert(extractRefund(m, invoiceInfo)).amount(m.getRefundAmount()).currency(m.getRefundCurrency()));
            case INVOICE_PAYMENT_REFUND_STATUS_CHANGED:
                return resolveRefundStatusChanged(m, invoiceInfo);
            default:
                throw new UnsupportedOperationException("Unknown event type " + m.getEventType());
        }
    }

    private com.rbkmoney.damsel.domain.InvoicePaymentRefund extractRefund(InvoicingMessage message, Invoice invoiceInfo) {
        return invoiceInfo.getPayments().stream()
                .filter(p -> p.getPayment().getId().equals(message.getPaymentId()))
                .findFirst().orElseThrow()
                .getRefunds().stream()
                .filter(r -> r.getId().equals(message.getRefundId()))
                .findFirst().orElseThrow();
    }

    private com.rbkmoney.damsel.domain.InvoicePayment extractPayment(InvoicingMessage message, Invoice invoiceInfo) {
        return invoiceInfo.getPayments().stream()
                .map(InvoicePayment::getPayment)
                .filter(p -> p.getId().equals(message.getPaymentId()))
                .findFirst().orElseThrow();
    }

    private Event resolveInvoiceStatusChanged(InvoicingMessage message, Invoice invoiceInfo) {
        com.rbkmoney.swag_webhook_events.model.Invoice convertedInvoice = invoiceConverter.convert(invoiceInfo.getInvoice());
        switch (message.getInvoiceStatus()) {
            case unpaid: return new InvoiceCreated().invoice(convertedInvoice);
            case paid: return new InvoicePaid().invoice(convertedInvoice);
            case cancelled: return new InvoiceCancelled().invoice(convertedInvoice);
            case fulfilled: return new InvoiceFulfilled().invoice(convertedInvoice);
            default: throw new UnsupportedOperationException("Unknown invoice status " + message.getInvoiceStatus());
        }
    }

    private Event resolvePaymentStatusChanged(InvoicingMessage message, Invoice invoiceInfo) {
        com.rbkmoney.swag_webhook_events.model.Invoice convertedInvoice = invoiceConverter.convert(invoiceInfo.getInvoice());
        Payment convertedPayment = paymentConverter.convert(extractPayment(message, invoiceInfo)).fee(message.getPaymentFee());
        switch (message.getPaymentStatus()) {
            case pending: return new PaymentStarted().invoice(convertedInvoice).payment(convertedPayment);
            case processed: return new PaymentProcessed().invoice(convertedInvoice).payment(convertedPayment);
            case captured: return new PaymentCaptured().invoice(convertedInvoice).payment(convertedPayment);
            case cancelled: return new PaymentCancelled().invoice(convertedInvoice).payment(convertedPayment);
            case refunded: return new PaymentRefunded().invoice(convertedInvoice).payment(convertedPayment);
            case failed: return new PaymentFailed().invoice(convertedInvoice).payment(convertedPayment);
            default: throw new UnsupportedOperationException("Unknown payment status " + message.getPaymentStatus());
        }
    }

    private Event resolveRefundStatusChanged(InvoicingMessage message, Invoice invoiceInfo) {
        com.rbkmoney.swag_webhook_events.model.Invoice convertedInvoice = invoiceConverter.convert(invoiceInfo.getInvoice());
        Payment convertedPayment = paymentConverter.convert(extractPayment(message, invoiceInfo)).fee(message.getPaymentFee());
        Refund convertedRefund = refundConverter.convert(extractRefund(message, invoiceInfo)).amount(message.getRefundAmount()).currency(message.getRefundCurrency());
        switch (message.getRefundStatus()) {
            case pending: return new RefundCreated().invoice(convertedInvoice).payment(convertedPayment).refund(convertedRefund);
            case succeeded: return new RefundSucceeded().invoice(convertedInvoice).payment(convertedPayment).refund(convertedRefund);
            case failed: return new RefundFailed().invoice(convertedInvoice).payment(convertedPayment).refund(convertedRefund);
            default: throw new UnsupportedOperationException("Unknown refund status " + message.getRefundStatus());
        }
    }
}