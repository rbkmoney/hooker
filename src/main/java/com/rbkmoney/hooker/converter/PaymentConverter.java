package com.rbkmoney.hooker.converter;

import com.rbkmoney.damsel.base.Rational;
import com.rbkmoney.damsel.domain.InvoiceCart;
import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.payment_processing.InvoicePayment;
import com.rbkmoney.hooker.model.FeeType;
import com.rbkmoney.hooker.utils.CashFlowUtils;
import com.rbkmoney.hooker.utils.ErrorUtils;
import com.rbkmoney.hooker.utils.PaymentToolUtils;
import com.rbkmoney.hooker.utils.TimeUtils;
import com.rbkmoney.swag_webhook_events.model.Allocation;
import com.rbkmoney.swag_webhook_events.model.AllocationTransaction;
import com.rbkmoney.swag_webhook_events.model.ClientInfo;
import com.rbkmoney.swag_webhook_events.model.ContactInfo;
import com.rbkmoney.swag_webhook_events.model.CustomerPayer;
import com.rbkmoney.swag_webhook_events.model.PaymentResourcePayer;
import com.rbkmoney.swag_webhook_events.model.RecurrentPayer;
import com.rbkmoney.swag_webhook_events.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PaymentConverter implements Converter<InvoicePayment, Payment> {

    private final MetadataDeserializer deserializer;

    @Override
    public Payment convert(InvoicePayment sourceWrapper) {
        var source = sourceWrapper.getPayment();

        Payment target = new Payment()
                .id(source.getId())
                .createdAt(TimeUtils.toOffsetDateTime(source.getCreatedAt()))
                .status(Payment.StatusEnum.fromValue(source.getStatus().getSetField().getFieldName()))
                .amount(source.getCost().getAmount())
                .currency(source.getCost().getCurrency().getSymbolicCode())
                .metadata(getMetadata(source))
                .fee(getFee(sourceWrapper))
                .rrn(getRrn(sourceWrapper))
                .allocation(getAllocation(sourceWrapper));

        if (source.getStatus().isSetFailed()) {
            setErrorDetails(source, target);
        } else if (source.getStatus().isSetCaptured()) {
            setCapturedParams(source, target);
        }

        if (source.getPayer().isSetPaymentResource()) {
            setResourcePaymentTool(source, target);
        } else if (source.getPayer().isSetCustomer()) {
            setCustomerPaymentTool(source, target);
        } else if (source.getPayer().isSetRecurrent()) {
            setRecurrentPaymentTool(source, target);
        }

        return target;
    }

    private Object getMetadata(com.rbkmoney.damsel.domain.InvoicePayment source) {
        return source.isSetContext() ? deserializer.deserialize(source.getContext().getData()) : null;
    }

    private Long getFee(InvoicePayment sourceWrapper) {
        return sourceWrapper.isSetCashFlow()
                ? CashFlowUtils.getFees(sourceWrapper.getDeprecatedCashFlow()).getOrDefault(FeeType.FEE, 0L) :
                0L; // TODO ???
    }

    private String getRrn(InvoicePayment sourceWrapper) {
        return isSetAdditionalInfo(sourceWrapper) ? getAdditionalInfo(sourceWrapper).getRrn() : null;
    }

    private void setErrorDetails(com.rbkmoney.damsel.domain.InvoicePayment source, Payment target) {
        target.setError(ErrorUtils.getPaymentError(source.getStatus().getFailed().getFailure()));
    }

    private void setCapturedParams(com.rbkmoney.damsel.domain.InvoicePayment source, Payment target) {
        InvoicePaymentCaptured invoicePaymentCaptured = source.getStatus().getCaptured();
        if (invoicePaymentCaptured.isSetCost()) {
            target.setAmount(invoicePaymentCaptured.getCost().getAmount());
            target.setCurrency(invoicePaymentCaptured.getCost().getCurrency().getSymbolicCode());
        }
    }

    private void setResourcePaymentTool(com.rbkmoney.damsel.domain.InvoicePayment source, Payment target) {
        com.rbkmoney.damsel.domain.PaymentResourcePayer payerOrigin = source.getPayer().getPaymentResource();
        DisposablePaymentResource resourceOrigin = payerOrigin.getResource();
        PaymentTool paymentTool = resourceOrigin.getPaymentTool();
        target.paymentToolToken(PaymentToolUtils.getPaymentToolToken(paymentTool))
                .paymentSession(resourceOrigin.getPaymentSessionId())
                .contactInfo(new PaymentContactInfo()
                        .email(payerOrigin.getContactInfo().getEmail())
                        .phoneNumber(payerOrigin.getContactInfo().getPhoneNumber()))
                .ip(resourceOrigin.isSetClientInfo() ? resourceOrigin.getClientInfo().getIpAddress() : null)
                .fingerprint(resourceOrigin.isSetClientInfo() ? resourceOrigin.getClientInfo().getFingerprint() : null)
                .payer(new PaymentResourcePayer()
                        .paymentSession(resourceOrigin.getPaymentSessionId())
                        .paymentToolToken(target.getPaymentToolToken())
                        .contactInfo(new ContactInfo()
                                .email(payerOrigin.getContactInfo().getEmail())
                                .phoneNumber(payerOrigin.getContactInfo().getPhoneNumber()))
                        .clientInfo(new ClientInfo()
                                .ip(resourceOrigin.isSetClientInfo() ? resourceOrigin.getClientInfo().getIpAddress() :
                                        null)
                                .fingerprint(resourceOrigin.isSetClientInfo()
                                        ? resourceOrigin.getClientInfo().getFingerprint() : null))
                        .paymentToolDetails(PaymentToolUtils.getPaymentToolDetails(paymentTool)));
    }

    private void setRecurrentPaymentTool(com.rbkmoney.damsel.domain.InvoicePayment source, Payment target) {
        com.rbkmoney.damsel.domain.RecurrentPayer recurrentParentOrigin = source.getPayer().getRecurrent();
        target.contactInfo(new PaymentContactInfo()
                .email(recurrentParentOrigin.getContactInfo().getEmail())
                .phoneNumber(recurrentParentOrigin.getContactInfo().getPhoneNumber()))
                .payer(new RecurrentPayer()
                        .recurrentParentPayment(new PaymentRecurrentParent()
                                .invoiceID(recurrentParentOrigin.getRecurrentParent().getInvoiceId())
                                .paymentID(recurrentParentOrigin.getRecurrentParent().getPaymentId()))
                        .contactInfo(new ContactInfo()
                                .email(recurrentParentOrigin.getContactInfo().getEmail())
                                .phoneNumber(recurrentParentOrigin.getContactInfo().getPhoneNumber())))
                .contactInfo(new PaymentContactInfo()
                        .email(recurrentParentOrigin.getContactInfo().getEmail())
                        .phoneNumber(recurrentParentOrigin.getContactInfo().getPhoneNumber()));
    }

    private void setCustomerPaymentTool(com.rbkmoney.damsel.domain.InvoicePayment source, Payment target) {
        com.rbkmoney.damsel.domain.CustomerPayer customerPayerOrigin = source.getPayer().getCustomer();
        target.paymentToolToken(PaymentToolUtils.getPaymentToolToken(customerPayerOrigin.getPaymentTool()))
                .contactInfo(new PaymentContactInfo()
                        .email(customerPayerOrigin.getContactInfo().getEmail())
                        .phoneNumber(customerPayerOrigin.getContactInfo().getPhoneNumber()))
                .payer(new CustomerPayer()
                        .customerID(source.getPayer().getCustomer().getCustomerId()));
    }

    private boolean isSetAdditionalInfo(InvoicePayment sourceWrapper) {
        return (!sourceWrapper.getSessions().isEmpty())
                && sourceWrapper.getSessions().get(0).isSetTransactionInfo()
                && sourceWrapper.getSessions().get(0).getTransactionInfo().isSetAdditionalInfo();
    }

    private AdditionalTransactionInfo getAdditionalInfo(InvoicePayment sourceWrapper) {
        return sourceWrapper.getSessions().get(0).getTransactionInfo().getAdditionalInfo();
    }

    private Allocation getAllocation(InvoicePayment sourceWrapper) {
        if (sourceWrapper.isSetAllocaton()) {
            Allocation result = new Allocation();
            var transactions = sourceWrapper.getAllocaton().getTransactions();
            List<AllocationTransaction> allocationTransactions = transactions.stream()
                    .map(this::buildAllocationTransaction)
                    .collect(Collectors.toList());
            result.addAll(allocationTransactions);
            return result;
        }
        return null;
    }

    private AllocationTransaction buildAllocationTransaction(
            com.rbkmoney.damsel.domain.AllocationTransaction allocationTransaction) {
        if (allocationTransaction.isSetBody()) {
            return buildAllocationBodyTotal(allocationTransaction);
        } else {
            return buildAllocationBodyAmount(allocationTransaction);
        }
    }

    private AllocationBodyTotal buildAllocationBodyTotal(
            com.rbkmoney.damsel.domain.AllocationTransaction allocationTransaction) {
        AllocationTransactionBodyTotal body = allocationTransaction.getBody();
        AllocationBodyTotal allocationBodyTotal = new AllocationBodyTotal();
        allocationBodyTotal.setAllocationBodyType(AllocationTransaction.AllocationBodyTypeEnum.ALLOCATIONBODYTOTAL);
        allocationBodyTotal.setTotal(body.getTotal().getAmount());
        if (allocationTransaction.isSetAmount()) {
            allocationBodyTotal.setAmount(allocationTransaction.getAmount().getAmount());
            allocationBodyTotal.setCurrency(allocationTransaction.getAmount().getCurrency().getSymbolicCode());
        }
        allocationBodyTotal.setTarget(buildTarget(allocationTransaction));
        if (body.isSetFee()) {
            AllocationTransactionFeeShare fee = body.getFee();
            AllocationFeeShare allocationFeeShare = new AllocationFeeShare();
            allocationFeeShare.setAllocationFeeType(AllocationFee.AllocationFeeTypeEnum.ALLOCATIONFEESHARE);
            if (fee.isSetParts()) {
                Rational parts = fee.getParts();
                allocationFeeShare.setShare(
                        new Decimal()
                                .m(parts.getP())
                                .exp(parts.getQ())
                );
            }
            allocationBodyTotal.setFee(allocationFeeShare);
        } else {
            AllocationFeeFixed allocationFeeFixed = new AllocationFeeFixed();
            allocationFeeFixed.setAllocationFeeType(AllocationFee.AllocationFeeTypeEnum.ALLOCATIONFEEFIXED);
            allocationFeeFixed.setAmount(body.getFeeAmount().getAmount());
        }
        allocationBodyTotal.setCart(buildInvoiceCart(allocationTransaction));
        return allocationBodyTotal;
    }

    private AllocationTarget buildTarget(com.rbkmoney.damsel.domain.AllocationTransaction sourceAllocationTransaction) {
        if (sourceAllocationTransaction.isSetTarget()) {
            AllocationTargetShop target = new AllocationTargetShop();
            target.setAllocationTargetType(AllocationTarget.AllocationTargetTypeEnum.ALLOCATIONTARGETSHOP);
            target.setShopID(sourceAllocationTransaction.getTarget().getShop().getShopId());
            return target;
        }
        return null;
    }

    private com.rbkmoney.swag_webhook_events.model.InvoiceCart buildInvoiceCart(
            com.rbkmoney.damsel.domain.AllocationTransaction sourceAllocationTransaction) {
        if (sourceAllocationTransaction.isSetDetails() && sourceAllocationTransaction.getDetails().isSetCart()) {
            InvoiceCart invoiceCartSource = sourceAllocationTransaction.getDetails().getCart();
            var invoiceCart = new com.rbkmoney.swag_webhook_events.model.InvoiceCart();
            List<InvoiceCartLine> invoiceCartLines = invoiceCartSource.getLines().stream()
                    .map(this::buildInvoiceCartLine)
                    .collect(Collectors.toList());
            invoiceCart.addAll(invoiceCartLines);
            return invoiceCart;
        }
        return null;
    }

    private InvoiceCartLine buildInvoiceCartLine(InvoiceLine invoiceLine) {
        InvoiceCartLine invoiceCartLine = new InvoiceCartLine();
        invoiceCartLine.setPrice(invoiceLine.getPrice().getAmount());
        invoiceCartLine.setProduct(invoiceLine.getProduct());
        invoiceCartLine.setQuantity((long) invoiceLine.getQuantity());
        return invoiceCartLine;
    }

    private AllocationBodyAmount buildAllocationBodyAmount(
            com.rbkmoney.damsel.domain.AllocationTransaction allocationTransaction) {
        AllocationBodyAmount allocationBodyAmount = new AllocationBodyAmount();
        allocationBodyAmount.setAllocationBodyType(AllocationTransaction.AllocationBodyTypeEnum.ALLOCATIONBODYAMOUNT);
        if (allocationTransaction.isSetAmount()) {
            allocationBodyAmount.setAmount(allocationTransaction.getAmount().getAmount());
            allocationBodyAmount.setCurrency(allocationTransaction.getAmount().getCurrency().getSymbolicCode());
        }
        allocationBodyAmount.setTarget(buildTarget(allocationTransaction));
        allocationBodyAmount.setCart(buildInvoiceCart(allocationTransaction));
        return allocationBodyAmount;
    }
}
