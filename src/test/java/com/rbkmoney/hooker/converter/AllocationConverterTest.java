package com.rbkmoney.hooker.converter;

import com.rbkmoney.damsel.domain.Allocation;
import com.rbkmoney.damsel.domain.AllocationTransaction;
import com.rbkmoney.geck.serializer.kit.mock.MockMode;
import com.rbkmoney.geck.serializer.kit.mock.MockTBaseProcessor;
import com.rbkmoney.geck.serializer.kit.tbase.TBaseHandler;
import com.rbkmoney.hooker.AbstractIntegrationTest;
import com.rbkmoney.swag_webhook_events.model.*;
import com.rbkmoney.swag_webhook_events.model.AllocationTransaction.AllocationBodyTypeEnum;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

import static java.util.List.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AllocationConverterTest extends AbstractIntegrationTest {

    @Autowired
    private AllocationConverter converter;

    @Test
    public void testConvertAllocationWithBody() throws IOException {
        MockTBaseProcessor mockTBaseProcessor = new MockTBaseProcessor(MockMode.RANDOM, 15, 1);
        com.rbkmoney.damsel.domain.AllocationTransaction allocationTransaction =
                mockTBaseProcessor.process(new com.rbkmoney.damsel.domain.AllocationTransaction(), new TBaseHandler<>(
                        com.rbkmoney.damsel.domain.AllocationTransaction.class));
        allocationTransaction.setBody(mockTBaseProcessor
                .process(new com.rbkmoney.damsel.domain.AllocationTransactionBodyTotal(), new TBaseHandler<>(
                        com.rbkmoney.damsel.domain.AllocationTransactionBodyTotal.class)));
        allocationTransaction.getBody().setFee(mockTBaseProcessor
                .process(new com.rbkmoney.damsel.domain.AllocationTransactionFeeShare(), new TBaseHandler<>(
                        com.rbkmoney.damsel.domain.AllocationTransactionFeeShare.class)));
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
        List<AllocationTransaction> allocationTransactions = of(allocationTransaction);
        allocation.setTransactions(allocationTransactions);

        var target = converter.convert(allocation);

        assertNotNull(target);
        assertEquals(1, target.size());

        com.rbkmoney.swag_webhook_events.model.AllocationTransaction actualAllocationTransaction = target.get(0);
        assertEquals(AllocationBodyTypeEnum.ALLOCATIONBODYTOTAL,
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
    public void testConvertAllocationWithoutBody() throws IOException {
        MockTBaseProcessor mockTBaseProcessor = new MockTBaseProcessor(MockMode.RANDOM, 15, 1);
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

        var target = converter.convert(allocation);

        assertNotNull(target);
        assertEquals(1, target.size());

        com.rbkmoney.swag_webhook_events.model.AllocationTransaction actualAllocationTransaction = target.get(0);
        assertEquals(AllocationBodyTypeEnum.ALLOCATIONBODYAMOUNT,
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