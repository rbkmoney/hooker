package com.rbkmoney.hooker.converter;

import com.rbkmoney.damsel.base.Rational;
import com.rbkmoney.damsel.domain.Allocation;
import com.rbkmoney.damsel.domain.AllocationTransactionBodyTotal;
import com.rbkmoney.damsel.domain.AllocationTransactionFeeShare;
import com.rbkmoney.damsel.domain.InvoiceLine;
import com.rbkmoney.swag_webhook_events.model.*;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AllocationConverter implements Converter<Allocation, com.rbkmoney.swag_webhook_events.model.Allocation> {

    @Override
    public com.rbkmoney.swag_webhook_events.model.Allocation convert(Allocation allocation) {
        var transactions = allocation.getTransactions();
        List<AllocationTransaction> allocationTransactions = transactions.stream()
                .map(this::convertAllocationTransaction)
                .collect(Collectors.toList());
        com.rbkmoney.swag_webhook_events.model.Allocation
                result = new com.rbkmoney.swag_webhook_events.model.Allocation();
        result.addAll(allocationTransactions);
        return result;
    }

    private AllocationTransaction convertAllocationTransaction(
            com.rbkmoney.damsel.domain.AllocationTransaction allocationTransaction) {
        if (allocationTransaction.isSetBody()) {
            return convertAllocationBodyTotal(allocationTransaction);
        } else {
            return convertAllocationBodyAmount(allocationTransaction);
        }
    }

    private AllocationBodyTotal convertAllocationBodyTotal(
            com.rbkmoney.damsel.domain.AllocationTransaction allocationTransaction) {
        AllocationTransactionBodyTotal body = allocationTransaction.getBody();
        AllocationBodyTotal allocationBodyTotal = new AllocationBodyTotal();
        allocationBodyTotal.setAllocationBodyType(AllocationTransaction.AllocationBodyTypeEnum.ALLOCATIONBODYTOTAL);
        allocationBodyTotal.setTotal(body.getTotal().getAmount());
        if (allocationTransaction.isSetAmount()) {
            allocationBodyTotal.setAmount(allocationTransaction.getAmount().getAmount());
            allocationBodyTotal.setCurrency(allocationTransaction.getAmount().getCurrency().getSymbolicCode());
        }
        allocationBodyTotal.setTarget(convertTarget(allocationTransaction));
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
        allocationBodyTotal.setCart(convertInvoiceCart(allocationTransaction));
        return allocationBodyTotal;
    }

    private AllocationTarget convertTarget(
            com.rbkmoney.damsel.domain.AllocationTransaction sourceAllocationTransaction) {
        if (sourceAllocationTransaction.isSetTarget()) {
            AllocationTargetShop target = new AllocationTargetShop();
            target.setAllocationTargetType(AllocationTarget.AllocationTargetTypeEnum.ALLOCATIONTARGETSHOP);
            target.setShopID(sourceAllocationTransaction.getTarget().getShop().getShopId());
            return target;
        }
        return null;
    }

    private InvoiceCart convertInvoiceCart(
            com.rbkmoney.damsel.domain.AllocationTransaction sourceAllocationTransaction) {
        if (sourceAllocationTransaction.isSetDetails() && sourceAllocationTransaction.getDetails().isSetCart()) {
            com.rbkmoney.damsel.domain.InvoiceCart invoiceCartSource =
                    sourceAllocationTransaction.getDetails().getCart();
            var invoiceCart = new InvoiceCart();
            List<InvoiceCartLine> invoiceCartLines = invoiceCartSource.getLines().stream()
                    .map(this::convertInvoiceCartLine)
                    .collect(Collectors.toList());
            invoiceCart.addAll(invoiceCartLines);
            return invoiceCart;
        }
        return null;
    }

    private InvoiceCartLine convertInvoiceCartLine(InvoiceLine invoiceLine) {
        InvoiceCartLine invoiceCartLine = new InvoiceCartLine();
        invoiceCartLine.setPrice(invoiceLine.getPrice().getAmount());
        invoiceCartLine.setProduct(invoiceLine.getProduct());
        invoiceCartLine.setQuantity((long) invoiceLine.getQuantity());
        return invoiceCartLine;
    }

    private AllocationBodyAmount convertAllocationBodyAmount(
            com.rbkmoney.damsel.domain.AllocationTransaction allocationTransaction) {
        AllocationBodyAmount allocationBodyAmount = new AllocationBodyAmount();
        allocationBodyAmount.setAllocationBodyType(AllocationTransaction.AllocationBodyTypeEnum.ALLOCATIONBODYAMOUNT);
        if (allocationTransaction.isSetAmount()) {
            allocationBodyAmount.setAmount(allocationTransaction.getAmount().getAmount());
            allocationBodyAmount.setCurrency(allocationTransaction.getAmount().getCurrency().getSymbolicCode());
        }
        allocationBodyAmount.setTarget(convertTarget(allocationTransaction));
        allocationBodyAmount.setCart(convertInvoiceCart(allocationTransaction));
        return allocationBodyAmount;
    }
}
