package com.rbkmoney.hooker.service;

import com.rbkmoney.damsel.domain.InvoicePaymentRefund;
import com.rbkmoney.damsel.payment_processing.Invoice;
import com.rbkmoney.damsel.payment_processing.*;
import com.rbkmoney.hooker.converter.InvoiceConverter;
import com.rbkmoney.hooker.converter.PaymentConverter;
import com.rbkmoney.hooker.converter.RefundConverter;
import com.rbkmoney.hooker.exception.NotFoundException;
import com.rbkmoney.hooker.exception.RemoteHostException;
import com.rbkmoney.hooker.model.InvoicingMessage;
import com.rbkmoney.hooker.utils.TimeUtils;
import com.rbkmoney.swag_webhook_events.model.Event;
import com.rbkmoney.swag_webhook_events.model.InvoiceCreated;
import com.rbkmoney.swag_webhook_events.model.*;
import lombok.RequiredArgsConstructor;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

import java.sql.Ref;
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

    @Override
    public Event getByMessage(InvoicingMessage message) {
        try {
            Invoice invoiceInfo = invoicingClient.get(userInfo, message.getInvoiceId(), new EventRange().setLimit(message.getSequenceId().intValue()));
            return resolveEvent(message, invoiceInfo)
                    .eventID(message.getEventId().intValue())
                    .occuredAt(TimeUtils.toOffsetDateTime(message.getEventTime()))
                    .topic(Event.TopicEnum.INVOICESTOPIC);
        } catch (InvoiceNotFound e) {
            throw new NotFoundException("Invoice not found, invoiceId=" + message.getInvoiceId());
        } catch (TException e) {
            throw new RemoteHostException(e);
        }
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
                com.rbkmoney.damsel.domain.InvoicePayment payment = extractPayment(m, invoiceInfo);
                InvoicePaymentRefund refund = extractRefund(m, invoiceInfo);
                Refund convertedRefund = refundConverter.convert(refund);
                setCash(convertedRefund, payment, refund);
                return new RefundCreated()
                        .invoice(invoiceConverter.convert(invoiceInfo.getInvoice()))
                        .payment(paymentConverter.convert(payment).fee(m.getPaymentFee()))
                        .refund(convertedRefund);
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
                .findFirst()
                .orElseThrow(() ->
                        new NotFoundException(String.format("Refund not found, invoiceId=%s, paymentId=%s, refundId=%s",
                                message.getInvoiceId(), message.getPaymentId(), message.getRefundId())));
    }

    private com.rbkmoney.damsel.domain.InvoicePayment extractPayment(InvoicingMessage message, Invoice invoiceInfo) {
        return invoiceInfo.getPayments().stream()
                .map(InvoicePayment::getPayment)
                .filter(p -> p.getId().equals(message.getPaymentId()))
                .findFirst().orElseThrow(() ->
                        new NotFoundException(String.format("Payment not found, invoiceId=%s, paymentId=%s",
                                message.getInvoiceId(), message.getPaymentId())));
    }

    private Event resolveInvoiceStatusChanged(InvoicingMessage message, Invoice invoiceInfo) {
        com.rbkmoney.swag_webhook_events.model.Invoice convertedInvoice = invoiceConverter.convert(invoiceInfo.getInvoice());
        switch (message.getInvoiceStatus()) {
            case UNPAID: return new InvoiceCreated().invoice(convertedInvoice);
            case PAID: return new InvoicePaid().invoice(convertedInvoice);
            case CANCELLED: return new InvoiceCancelled().invoice(convertedInvoice);
            case FULFILLED: return new InvoiceFulfilled().invoice(convertedInvoice);
            default: throw new UnsupportedOperationException("Unknown invoice status " + message.getInvoiceStatus());
        }
    }

    private Event resolvePaymentStatusChanged(InvoicingMessage message, Invoice invoiceInfo) {
        com.rbkmoney.swag_webhook_events.model.Invoice convertedInvoice = invoiceConverter.convert(invoiceInfo.getInvoice());
        Payment convertedPayment = paymentConverter.convert(extractPayment(message, invoiceInfo)).fee(message.getPaymentFee());
        switch (message.getPaymentStatus()) {
            case PENDING: return new PaymentStarted().invoice(convertedInvoice).payment(convertedPayment);
            case PROCESSED: return new PaymentProcessed().invoice(convertedInvoice).payment(convertedPayment);
            case CAPTURED: return new PaymentCaptured().invoice(convertedInvoice).payment(convertedPayment);
            case CANCELLED: return new PaymentCancelled().invoice(convertedInvoice).payment(convertedPayment);
            case REFUNDED: return new PaymentRefunded().invoice(convertedInvoice).payment(convertedPayment);
            case FAILED: return new PaymentFailed().invoice(convertedInvoice).payment(convertedPayment);
            default: throw new UnsupportedOperationException("Unknown payment status " + message.getPaymentStatus());
        }
    }

    private Event resolveRefundStatusChanged(InvoicingMessage message, Invoice invoiceInfo) {
        com.rbkmoney.swag_webhook_events.model.Invoice convertedInvoice = invoiceConverter.convert(invoiceInfo.getInvoice());
        com.rbkmoney.damsel.domain.InvoicePayment payment = extractPayment(message, invoiceInfo);
        Payment convertedPayment = paymentConverter.convert(payment).fee(message.getPaymentFee());
        InvoicePaymentRefund refund = extractRefund(message, invoiceInfo);
        Refund convertedRefund = refundConverter.convert(refund);
        setCash(convertedRefund, payment, refund);
        switch (message.getRefundStatus()) {
            case PENDING: return new RefundCreated().invoice(convertedInvoice).payment(convertedPayment).refund(convertedRefund);
            case SUCCEEDED: return new RefundSucceeded().invoice(convertedInvoice).payment(convertedPayment).refund(convertedRefund);
            case FAILED: return new RefundFailed().invoice(convertedInvoice).payment(convertedPayment).refund(convertedRefund);
            default: throw new UnsupportedOperationException("Unknown refund status " + message.getRefundStatus());
        }
    }

    private void setCash(Refund convertedRefund, com.rbkmoney.damsel.domain.InvoicePayment payment, InvoicePaymentRefund refund){
        convertedRefund.amount(refund.isSetCash() ? refund.getCash().getAmount() : payment.getCost().getAmount())
                .currency(refund.isSetCash() ? refund.getCash().getCurrency().getSymbolicCode() : payment.getCost().getCurrency().getSymbolicCode());
    }
}
