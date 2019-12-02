package com.rbkmoney.hooker.service;

import com.rbkmoney.damsel.payment_processing.*;
import com.rbkmoney.hooker.converter.InvoiceConverter;
import com.rbkmoney.hooker.converter.PaymentConverter;
import com.rbkmoney.hooker.converter.RefundConverter;
import com.rbkmoney.hooker.exception.NotFoundException;
import com.rbkmoney.hooker.exception.RemoteHostException;
import com.rbkmoney.hooker.model.InvoicingMessage;
import com.rbkmoney.hooker.utils.TimeUtils;
import com.rbkmoney.swag_webhook_events.model.Event;
import com.rbkmoney.swag_webhook_events.model.Invoice;
import com.rbkmoney.swag_webhook_events.model.InvoiceCreated;
import com.rbkmoney.swag_webhook_events.model.*;
import lombok.RequiredArgsConstructor;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvoicingEventService implements EventService<InvoicingMessage> {

    private final UserInfo userInfo = new UserInfo("hooker", UserType.service_user(new ServiceUser()));
    private final InvoicingSrv.Iface invoicingClient;
    private final InvoiceConverter invoiceConverter;
    private final PaymentConverter paymentConverter;
    private final RefundConverter refundConverter;

    @Override
    public Event getByMessage(InvoicingMessage message) {
        try {
            var invoiceInfo = invoicingClient.get(userInfo, message.getInvoiceId(), getEventRange(message.getSequenceId().intValue()));
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

    private Event resolveEvent(InvoicingMessage m, com.rbkmoney.damsel.payment_processing.Invoice invoiceInfo) {
        switch (m.getEventType()) {
            case INVOICE_CREATED:
                return new InvoiceCreated()
                        .invoice(getSwagInvoice(invoiceInfo));
            case INVOICE_STATUS_CHANGED:
                return resolveInvoiceStatusChanged(m, invoiceInfo);
            case INVOICE_PAYMENT_STARTED:
                return new PaymentStarted()
                        .invoice(getSwagInvoice(invoiceInfo))
                        .payment(getSwagPayment(m, invoiceInfo));
            case INVOICE_PAYMENT_STATUS_CHANGED:
                return resolvePaymentStatusChanged(m, invoiceInfo);
            case INVOICE_PAYMENT_REFUND_STARTED:
                return new RefundCreated()
                        .invoice(getSwagInvoice(invoiceInfo))
                        .payment(getSwagPayment(m, invoiceInfo))
                        .refund(getSwagRefund(m, invoiceInfo));
            case INVOICE_PAYMENT_REFUND_STATUS_CHANGED:
                return resolveRefundStatusChanged(m, invoiceInfo);
            default:
                throw new UnsupportedOperationException("Unknown event type " + m.getEventType());
        }
    }

    private Invoice getSwagInvoice(com.rbkmoney.damsel.payment_processing.Invoice invoiceInfo) {
        return invoiceConverter.convert(invoiceInfo.getInvoice());
    }

    private Event resolveInvoiceStatusChanged(InvoicingMessage message, com.rbkmoney.damsel.payment_processing.Invoice invoiceInfo) {
        Invoice swagInvoice = getSwagInvoice(invoiceInfo);
        switch (message.getInvoiceStatus()) {
            case UNPAID:
                return new InvoiceCreated().invoice(swagInvoice);
            case PAID:
                return new InvoicePaid().invoice(swagInvoice);
            case CANCELLED:
                return new InvoiceCancelled().invoice(swagInvoice);
            case FULFILLED:
                return new InvoiceFulfilled().invoice(swagInvoice);
            default:
                throw new UnsupportedOperationException("Unknown invoice status " + message.getInvoiceStatus());
        }
    }

    private Payment getSwagPayment(InvoicingMessage m, com.rbkmoney.damsel.payment_processing.Invoice invoiceInfo) {
        var damselPayment = extractPayment(m, invoiceInfo);

        return paymentConverter.convert(damselPayment);
    }

    private InvoicePayment extractPayment(InvoicingMessage message, com.rbkmoney.damsel.payment_processing.Invoice invoiceInfo) {
        return invoiceInfo.getPayments().stream()
                .filter(p -> p.getPayment().getId().equals(message.getPaymentId()))
                .findFirst()
                .orElseThrow(
                        () -> new NotFoundException(
                                String.format("Payment not found, invoiceId=%s, paymentId=%s", message.getInvoiceId(), message.getPaymentId())
                        )
                );
    }

    private Event resolvePaymentStatusChanged(InvoicingMessage message, com.rbkmoney.damsel.payment_processing.Invoice invoiceInfo) {
        Invoice swagInvoice = getSwagInvoice(invoiceInfo);
        Payment swagPayment = getSwagPayment(message, invoiceInfo);
        switch (message.getPaymentStatus()) {
            case PENDING:
                return new PaymentStarted().invoice(swagInvoice).payment(swagPayment);
            case PROCESSED:
                return new PaymentProcessed().invoice(swagInvoice).payment(swagPayment);
            case CAPTURED:
                return new PaymentCaptured().invoice(swagInvoice).payment(swagPayment);
            case CANCELLED:
                return new PaymentCancelled().invoice(swagInvoice).payment(swagPayment);
            case REFUNDED:
                return new PaymentRefunded().invoice(swagInvoice).payment(swagPayment);
            case FAILED:
                return new PaymentFailed().invoice(swagInvoice).payment(swagPayment);
            default:
                throw new UnsupportedOperationException("Unknown payment status " + message.getPaymentStatus());
        }
    }

    private Refund getSwagRefund(InvoicingMessage m, com.rbkmoney.damsel.payment_processing.Invoice invoiceInfo) {
        var damselPayment = extractPayment(m, invoiceInfo);
        var damselRefund = extractRefund(m, damselPayment);

        Refund swagRefund = refundConverter.convert(damselRefund);
        if (swagRefund.getAmount() == null) {
            swagRefund.setAmount(damselPayment.getPayment().getCost().getAmount());
            swagRefund.setCurrency(damselPayment.getPayment().getCost().getCurrency().getSymbolicCode());
        }
        return swagRefund;
    }

    private com.rbkmoney.damsel.payment_processing.InvoicePaymentRefund extractRefund(InvoicingMessage m, InvoicePayment damselPayment) {
        return damselPayment.getRefunds().stream()
                .filter(invoicePaymentRefund -> invoicePaymentRefund.getRefund().getId().equals(m.getRefundId()))
                .findFirst()
                .orElseThrow(
                        () -> new NotFoundException(
                                String.format(
                                        "Refund not found, invoiceId=%s, paymentId=%s, refundId=%s",
                                        m.getInvoiceId(), m.getPaymentId(), m.getRefundId()
                                )
                        )
                );
    }

    private Event resolveRefundStatusChanged(InvoicingMessage message, com.rbkmoney.damsel.payment_processing.Invoice invoiceInfo) {
        Invoice swagInvoice = getSwagInvoice(invoiceInfo);
        Payment swagPayment = getSwagPayment(message, invoiceInfo);
        Refund swagRefund = getSwagRefund(message, invoiceInfo);
        switch (message.getRefundStatus()) {
            case PENDING:
                return new RefundCreated().invoice(swagInvoice).payment(swagPayment).refund(swagRefund);
            case SUCCEEDED:
                return new RefundSucceeded().invoice(swagInvoice).payment(swagPayment).refund(swagRefund);
            case FAILED:
                return new RefundFailed().invoice(swagInvoice).payment(swagPayment).refund(swagRefund);
            default:
                throw new UnsupportedOperationException("Unknown refund status " + message.getRefundStatus());
        }
    }
}
