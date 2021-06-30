package com.rbkmoney.hooker.converter;

import com.rbkmoney.damsel.domain.Allocation;
import com.rbkmoney.damsel.domain.BankCard;
import com.rbkmoney.damsel.domain.InvoicePayment;
import com.rbkmoney.damsel.domain.PaymentTool;
import com.rbkmoney.geck.serializer.kit.mock.MockMode;
import com.rbkmoney.geck.serializer.kit.mock.MockTBaseProcessor;
import com.rbkmoney.geck.serializer.kit.tbase.TBaseHandler;
import com.rbkmoney.hooker.AbstractIntegrationTest;
import com.rbkmoney.swag_webhook_events.model.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

import static java.util.List.of;
import static org.junit.Assert.*;

public class PaymentConverterTest extends AbstractIntegrationTest {

    @Autowired
    private PaymentConverter converter;

    @Test
    public void testConvert() throws IOException {
        MockTBaseProcessor mockTBaseProcessor = new MockTBaseProcessor(MockMode.RANDOM, 15, 1);
        InvoicePayment source = mockTBaseProcessor
                .process(new InvoicePayment(), new TBaseHandler<>(InvoicePayment.class));
        source.setCreatedAt("2016-03-22T06:12:27Z");
        if (source.getPayer().isSetPaymentResource()) {
            source.getPayer().getPaymentResource().getResource()
                    .setPaymentTool(PaymentTool
                            .bank_card(mockTBaseProcessor.process(new BankCard(), new TBaseHandler<>(BankCard.class))));
        }
        Payment target = converter
                .convert(new com.rbkmoney.damsel.payment_processing.InvoicePayment(source, of(), of(), of(), of()));
        assertEquals(source.getId(), target.getId());
        assertEquals(source.getStatus().getSetField().getFieldName(), target.getStatus().getValue());
        if (source.getStatus().isSetCaptured() && source.getStatus().getCaptured().isSetCost()) {
            assertEquals(source.getStatus().getCaptured().getCost().getAmount(), target.getAmount().longValue());
            assertEquals(source.getStatus().getCaptured().getCost().getCurrency().getSymbolicCode(),
                    target.getCurrency());
        } else {
            assertEquals(source.getCost().getAmount(), target.getAmount().longValue());
            assertEquals(source.getCost().getCurrency().getSymbolicCode(), target.getCurrency());
        }
        if (source.getPayer().isSetCustomer()) {
            assertTrue(target.getPayer() instanceof CustomerPayer);
            assertEquals(source.getPayer().getCustomer().getCustomerId(),
                    ((CustomerPayer) target.getPayer()).getCustomerID());
        }
        if (source.getPayer().isSetPaymentResource()) {
            assertTrue(target.getPayer() instanceof PaymentResourcePayer);
            assertEquals(source.getPayer().getPaymentResource().getContactInfo().getEmail(),
                    ((PaymentResourcePayer) target.getPayer()).getContactInfo().getEmail());
            assertEquals(source.getPayer().getPaymentResource().getContactInfo().getPhoneNumber(),
                    ((PaymentResourcePayer) target.getPayer()).getContactInfo().getPhoneNumber());
            assertEquals(source.getPayer().getPaymentResource().getResource().getPaymentSessionId(),
                    ((PaymentResourcePayer) target.getPayer()).getPaymentSession());
        } else if (source.getPayer().isSetRecurrent()) {
            assertTrue(target.getPayer() instanceof RecurrentPayer);
            assertEquals(source.getPayer().getRecurrent().getRecurrentParent().getInvoiceId(),
                    ((RecurrentPayer) target.getPayer()).getRecurrentParentPayment().getInvoiceID());
        }
    }

    @Test
    public void testConvertAllocationPaymentWithBody() throws IOException {
        MockTBaseProcessor mockTBaseProcessor = new MockTBaseProcessor(MockMode.RANDOM, 15, 1);
        com.rbkmoney.damsel.payment_processing.InvoicePayment source = mockTBaseProcessor
                .process(new com.rbkmoney.damsel.payment_processing.InvoicePayment(), new TBaseHandler<>(
                        com.rbkmoney.damsel.payment_processing.InvoicePayment.class));
        if (source.isSetPayment()) {
            source.getPayment().setCreatedAt("2016-03-22T06:12:27Z");
            if (source.getPayment().getPayer().isSetPaymentResource()) {
                source.getPayment().getPayer().getPaymentResource().getResource()
                        .setPaymentTool(PaymentTool
                                .bank_card(mockTBaseProcessor
                                        .process(new BankCard(), new TBaseHandler<>(BankCard.class))));
            }
        }
        com.rbkmoney.damsel.domain.AllocationTransaction allocationTransaction =
                mockTBaseProcessor.process(new com.rbkmoney.damsel.domain.AllocationTransaction(), new TBaseHandler<>(
                        com.rbkmoney.damsel.domain.AllocationTransaction.class));
        allocationTransaction.setBody(mockTBaseProcessor
                .process(new com.rbkmoney.damsel.domain.AllocationTransactionBodyTotal(), new TBaseHandler<>(
                        com.rbkmoney.damsel.domain.AllocationTransactionBodyTotal.class)));
        allocationTransaction.setTarget(mockTBaseProcessor
                .process(new com.rbkmoney.damsel.domain.AllocationTransactionTarget(), new TBaseHandler<>(
                        com.rbkmoney.damsel.domain.AllocationTransactionTarget.class)));
        allocationTransaction.setDetails(mockTBaseProcessor
                .process(new com.rbkmoney.damsel.domain.AllocationTransactionDetails(), new TBaseHandler<>(
                        com.rbkmoney.damsel.domain.AllocationTransactionDetails.class)));
        com.rbkmoney.damsel.domain.InvoiceCart invoiceCart = new com.rbkmoney.damsel.domain.InvoiceCart();
        invoiceCart.setLines(
                of(mockTBaseProcessor.process(new com.rbkmoney.damsel.domain.InvoiceLine(), new TBaseHandler<>(
                        com.rbkmoney.damsel.domain.InvoiceLine.class))));
        allocationTransaction.getDetails().setCart(invoiceCart);
        Allocation allocation = new Allocation();
        List<com.rbkmoney.damsel.domain.AllocationTransaction> allocationTransactions = of(allocationTransaction);
        allocation.setTransactions(allocationTransactions);
        source.setAllocaton(allocation);

        Payment target = converter.convert(source);

        com.rbkmoney.swag_webhook_events.model.Allocation actualAllocation = target.getAllocation();
        assertNotNull(actualAllocation);
        assertEquals(1, actualAllocation.size());

        AllocationTransaction actualAllocationTransaction = actualAllocation.get(0);
        assertEquals(AllocationTransaction.AllocationBodyTypeEnum.ALLOCATIONBODYTOTAL,
                actualAllocationTransaction.getAllocationBodyType());
        AllocationBodyTotal allocationBodyTotal = (AllocationBodyTotal) actualAllocationTransaction;
        assertEquals(Long.valueOf(allocationTransaction.getBody().getTotal().getAmount()),
                allocationBodyTotal.getTotal());
        assertEquals(Long.valueOf(allocationTransaction.getAmount().getAmount()), allocationBodyTotal.getAmount());

        assertEquals(AllocationTarget.AllocationTargetTypeEnum.ALLOCATIONTARGETSHOP,
                actualAllocationTransaction.getTarget().getAllocationTargetType());
        AllocationTargetShop allocationTargetShop = (AllocationTargetShop) allocationBodyTotal.getTarget();
        assertEquals(allocationTransaction.getTarget().getShop().getShopId(), allocationTargetShop.getShopID());

        assertEquals(AllocationFee.AllocationFeeTypeEnum.ALLOCATIONFEESHARE,
                allocationBodyTotal.getFee().getAllocationFeeType());
        AllocationFeeShare allocationFeeShare = (AllocationFeeShare) allocationBodyTotal.getFee();
        assertEquals(Long.valueOf(allocationTransaction.getBody().getFee().getParts().getP()),
                allocationFeeShare.getShare().getM());
        assertEquals(Long.valueOf(allocationTransaction.getBody().getFee().getParts().getQ()),
                allocationFeeShare.getShare().getExp());

        assertEquals(
                Long.valueOf(allocationTransaction.getDetails().getCart().getLines().get(0).getPrice().getAmount()),
                allocationBodyTotal.getCart().get(0).getPrice());
        assertEquals(Long.valueOf(allocationTransaction.getDetails().getCart().getLines().get(0).getQuantity()),
                allocationBodyTotal.getCart().get(0).getQuantity());
        assertEquals(allocationTransaction.getDetails().getCart().getLines().get(0).getProduct(),
                allocationBodyTotal.getCart().get(0).getProduct());

    }

    @Test
    public void testConvertAllocationPaymentWithoutBody() throws IOException {
        MockTBaseProcessor mockTBaseProcessor = new MockTBaseProcessor(MockMode.RANDOM, 15, 1);
        com.rbkmoney.damsel.payment_processing.InvoicePayment source = mockTBaseProcessor
                .process(new com.rbkmoney.damsel.payment_processing.InvoicePayment(), new TBaseHandler<>(
                        com.rbkmoney.damsel.payment_processing.InvoicePayment.class));
        if (source.isSetPayment()) {
            source.getPayment().setCreatedAt("2016-03-22T06:12:27Z");
            if (source.getPayment().getPayer().isSetPaymentResource()) {
                source.getPayment().getPayer().getPaymentResource().getResource()
                        .setPaymentTool(PaymentTool
                                .bank_card(mockTBaseProcessor
                                        .process(new BankCard(), new TBaseHandler<>(BankCard.class))));
            }
        }
        com.rbkmoney.damsel.domain.AllocationTransaction allocationTransaction =
                mockTBaseProcessor.process(new com.rbkmoney.damsel.domain.AllocationTransaction(), new TBaseHandler<>(
                        com.rbkmoney.damsel.domain.AllocationTransaction.class));
        allocationTransaction.setBody(null);
        allocationTransaction.setTarget(mockTBaseProcessor
                .process(new com.rbkmoney.damsel.domain.AllocationTransactionTarget(), new TBaseHandler<>(
                        com.rbkmoney.damsel.domain.AllocationTransactionTarget.class)));
        allocationTransaction.setDetails(mockTBaseProcessor
                .process(new com.rbkmoney.damsel.domain.AllocationTransactionDetails(), new TBaseHandler<>(
                        com.rbkmoney.damsel.domain.AllocationTransactionDetails.class)));
        com.rbkmoney.damsel.domain.InvoiceCart invoiceCart = new com.rbkmoney.damsel.domain.InvoiceCart();
        invoiceCart.setLines(
                of(mockTBaseProcessor.process(new com.rbkmoney.damsel.domain.InvoiceLine(), new TBaseHandler<>(
                        com.rbkmoney.damsel.domain.InvoiceLine.class))));
        allocationTransaction.getDetails().setCart(invoiceCart);
        Allocation allocation = new Allocation();
        List<com.rbkmoney.damsel.domain.AllocationTransaction> allocationTransactions = of(allocationTransaction);
        allocation.setTransactions(allocationTransactions);
        source.setAllocaton(allocation);

        Payment target = converter.convert(source);

        com.rbkmoney.swag_webhook_events.model.Allocation actualAllocation = target.getAllocation();
        assertNotNull(actualAllocation);
        assertEquals(1, actualAllocation.size());

        AllocationTransaction actualAllocationTransaction = actualAllocation.get(0);
        assertEquals(AllocationTransaction.AllocationBodyTypeEnum.ALLOCATIONBODYAMOUNT,
                actualAllocationTransaction.getAllocationBodyType());
        AllocationBodyAmount allocationBodyAmount = (AllocationBodyAmount) actualAllocationTransaction;
        assertEquals(Long.valueOf(allocationTransaction.getAmount().getAmount()), allocationBodyAmount.getAmount());
        assertEquals(allocationTransaction.getAmount().getCurrency().getSymbolicCode(),
                allocationBodyAmount.getCurrency());

        assertEquals(AllocationTarget.AllocationTargetTypeEnum.ALLOCATIONTARGETSHOP,
                actualAllocationTransaction.getTarget().getAllocationTargetType());
        AllocationTargetShop allocationTargetShop = (AllocationTargetShop) allocationBodyAmount.getTarget();
        assertEquals(allocationTransaction.getTarget().getShop().getShopId(), allocationTargetShop.getShopID());

        assertEquals(
                Long.valueOf(allocationTransaction.getDetails().getCart().getLines().get(0).getPrice().getAmount()),
                allocationBodyAmount.getCart().get(0).getPrice());
        assertEquals(Long.valueOf(allocationTransaction.getDetails().getCart().getLines().get(0).getQuantity()),
                allocationBodyAmount.getCart().get(0).getQuantity());
        assertEquals(allocationTransaction.getDetails().getCart().getLines().get(0).getProduct(),
                allocationBodyAmount.getCart().get(0).getProduct());

    }
}
