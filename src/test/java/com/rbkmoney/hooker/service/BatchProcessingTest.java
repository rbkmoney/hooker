package com.rbkmoney.hooker.service;

import com.rbkmoney.damsel.base.Content;
import com.rbkmoney.damsel.domain.BankCard;
import com.rbkmoney.damsel.domain.Cash;
import com.rbkmoney.damsel.domain.ClientInfo;
import com.rbkmoney.damsel.domain.ContactInfo;
import com.rbkmoney.damsel.domain.CurrencyRef;
import com.rbkmoney.damsel.domain.DisposablePaymentResource;
import com.rbkmoney.damsel.domain.Invoice;
import com.rbkmoney.damsel.domain.InvoiceCart;
import com.rbkmoney.damsel.domain.InvoiceDetails;
import com.rbkmoney.damsel.domain.InvoiceLine;
import com.rbkmoney.damsel.domain.InvoicePayment;
import com.rbkmoney.damsel.domain.InvoicePaymentFlow;
import com.rbkmoney.damsel.domain.InvoicePaymentPending;
import com.rbkmoney.damsel.domain.InvoicePaymentProcessed;
import com.rbkmoney.damsel.domain.InvoicePaymentStatus;
import com.rbkmoney.damsel.domain.InvoiceStatus;
import com.rbkmoney.damsel.domain.InvoiceUnpaid;
import com.rbkmoney.damsel.domain.LegacyBankCardPaymentSystem;
import com.rbkmoney.damsel.domain.Payer;
import com.rbkmoney.damsel.domain.PaymentResourcePayer;
import com.rbkmoney.damsel.domain.PaymentTool;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoiceCreated;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentChangePayload;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentStarted;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentStatusChanged;
import com.rbkmoney.hooker.AbstractIntegrationTest;
import com.rbkmoney.hooker.dao.HookDao;
import com.rbkmoney.hooker.dao.impl.InvoicingMessageDaoImpl;
import com.rbkmoney.hooker.dao.impl.InvoicingQueueDao;
import com.rbkmoney.hooker.dao.impl.InvoicingTaskDao;
import com.rbkmoney.hooker.handler.poller.invoicing.AbstractInvoiceEventMapper;
import com.rbkmoney.hooker.model.EventInfo;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.model.InvoicingMessage;
import com.rbkmoney.hooker.model.InvoicingMessageEnum;
import com.rbkmoney.hooker.model.InvoicingMessageKey;
import com.rbkmoney.hooker.model.PaymentStatusEnum;
import com.rbkmoney.hooker.utils.BuildUtils;
import com.rbkmoney.hooker.utils.KeyUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class BatchProcessingTest extends AbstractIntegrationTest {

    @Autowired
    private HandlerManager handlerManager;

    @Autowired
    private BatchService batchService;

    @Autowired
    private InvoicingMessageDaoImpl messageDao;

    @Autowired
    private InvoicingTaskDao taskDao;

    @Autowired
    private InvoicingQueueDao invoicingQueueDao;

    @Autowired
    private HookDao hookDao;

    @Test
    public void testBatchProcess() {
        hookDao.create(BuildUtils.buildHook("partyId", "www.kek.ru", EventType.INVOICE_CREATED));

        LinkedHashMap<InvoicingMessageKey, InvoicingMessage> storage = new LinkedHashMap<>();

        InvoiceChange ic = getInvoiceCreated();
        EventInfo eventInfo = new EventInfo(null, "2016-03-22T06:12:27Z", "invoiceId", 1L, 1);
        Optional<AbstractInvoiceEventMapper> eventMapperOptional = handlerManager.getHandler(ic);
        assertTrue(eventMapperOptional.isPresent());
        AbstractInvoiceEventMapper invoiceEventMapper = eventMapperOptional.get();
        InvoicingMessage invoiceCreated = invoiceEventMapper.handle(ic, eventInfo, storage);
        assertNotNull(invoiceCreated);
        assertEquals("invoiceId", invoiceCreated.getInvoiceId());
        assertEquals(InvoicingMessageEnum.INVOICE, invoiceCreated.getType());
        storage.put(KeyUtils.key(invoiceCreated), invoiceCreated);

        InvoiceChange icPaymentStarted = getInvoicePaymentStarted();
        EventInfo eventInfoPaymentStarted = new EventInfo(null, "2016-03-22T06:12:27Z", "invoiceId", 1L, 2);
        Optional<AbstractInvoiceEventMapper> eventMapperPaymentStartedOptional =
                handlerManager.getHandler(icPaymentStarted);
        assertTrue(eventMapperPaymentStartedOptional.isPresent());
        AbstractInvoiceEventMapper invoicePaymentStartedEventMapper = eventMapperPaymentStartedOptional.get();
        InvoicingMessage paymentStarted =
                invoicePaymentStartedEventMapper.handle(icPaymentStarted, eventInfoPaymentStarted, storage);
        assertNotNull(paymentStarted);
        assertEquals(InvoicingMessageEnum.PAYMENT, paymentStarted.getType());
        assertEquals("partyId", paymentStarted.getPartyId());
        assertNotEquals(invoiceCreated.getChangeId(), paymentStarted.getChangeId());

        List<InvoicingMessage> messages = new ArrayList<>();

        storage.put(KeyUtils.key(invoiceCreated), invoiceCreated);
        storage.put(KeyUtils.key(paymentStarted), paymentStarted);
        messages.add(invoiceCreated);
        messages.add(paymentStarted);

        assertEquals(2, storage.size());
        assertEquals(2, messages.size());

        InvoiceChange icStatusChanged = getInvoicePaymentChangeStatus();
        EventInfo eventInfoPaymentStatusChanged = new EventInfo(null, "2016-03-22T06:12:27Z", "invoiceId", 1L, 3);
        Optional<AbstractInvoiceEventMapper> eventMapperPaymentStatusChangedOptional =
                handlerManager.getHandler(icStatusChanged);
        assertTrue(eventMapperPaymentStatusChangedOptional.isPresent());
        AbstractInvoiceEventMapper invoicePaymentStatusChangedEventMapper =
                eventMapperPaymentStatusChangedOptional.get();
        InvoicingMessage statusChanged =
                invoicePaymentStatusChangedEventMapper.handle(icStatusChanged, eventInfoPaymentStatusChanged, storage);
        assertNotNull(statusChanged);
        assertEquals("partyId", statusChanged.getPartyId());
        assertEquals(PaymentStatusEnum.PROCESSED, statusChanged.getPaymentStatus());
        assertNotEquals(statusChanged.getPaymentStatus(), paymentStarted.getPaymentStatus());
        storage.put(KeyUtils.key(statusChanged), statusChanged);
        messages.add(statusChanged);

        assertEquals(2, storage.size());
        assertEquals(3, messages.size());

        // not found message
        InvoiceChange icStatusNotFoundChanged = getInvoicePaymentChangeStatus();
        EventInfo eventInfoPaymentStatusNotFoundChanged =
                new EventInfo(null, "2016-03-22T06:12:27Z", "not_found", 1L, 3);
        Optional<AbstractInvoiceEventMapper> eventMapperPaymentStatusNotFoundChangedOptional =
                handlerManager.getHandler(icStatusNotFoundChanged);
        assertTrue(eventMapperPaymentStatusChangedOptional.isPresent());
        AbstractInvoiceEventMapper invoicePaymentStatusNotFoundChangedEventMapper =
                eventMapperPaymentStatusNotFoundChangedOptional.get();
        InvoicingMessage statusNotFoundChanged = invoicePaymentStatusNotFoundChangedEventMapper
                .handle(icStatusNotFoundChanged, eventInfoPaymentStatusNotFoundChanged, storage);
        assertNull(statusNotFoundChanged);

        batchService.process(messages);
        //
        InvoicingMessage invoiceCreatedFromDB = messageDao.getInvoicingMessage(KeyUtils.key(invoiceCreated));
        assertNotNull(invoiceCreatedFromDB);
        assertNotNull(invoiceCreatedFromDB.getId());
        assertNotNull(invoiceCreatedFromDB.getEventId());
        InvoicingMessage lastStateOfPayment = messageDao.getInvoicingMessage(KeyUtils.key(statusChanged));
        assertNotNull(lastStateOfPayment);
        assertNotEquals(lastStateOfPayment.getId(), invoiceCreatedFromDB.getId());
        InvoicingMessage theSameLastStateOfPayment = messageDao.getInvoicingMessage(KeyUtils.key(paymentStarted));
        assertNotNull(theSameLastStateOfPayment);
        Long messageId = theSameLastStateOfPayment.getId();
        assertEquals(messageId, lastStateOfPayment.getId());

        assertNotEquals(messageDao.getBy(Collections.singletonList(messageId - 1)).get(0).getPaymentStatus(),
                messageDao.getBy(Collections.singletonList(messageId)).get(0).getPaymentStatus());

        assertEquals(1, taskDao.getScheduled().size());
        assertEquals(1, invoicingQueueDao.getWithPolicies(Collections.singletonList(1L)).size());

        //test duplication
        batchService.process(messages);
        assertEquals(1, invoicingQueueDao.getWithPolicies(Collections.singletonList(1L)).size());
    }

    private InvoiceChange getInvoicePaymentStarted() {
        InvoicePayment payment = new InvoicePayment()
                .setId("1")
                .setCreatedAt("2016-03-22T06:12:27Z")
                .setStatus(InvoicePaymentStatus.pending(new InvoicePaymentPending()))
                .setPayer(Payer.payment_resource(
                        new PaymentResourcePayer()
                                .setResource(new DisposablePaymentResource()
                                        .setPaymentTool(PaymentTool.bank_card(new BankCard()
                                                .setToken("token")
                                                .setPaymentSystemDeprecated(LegacyBankCardPaymentSystem.amex)
                                                .setBin("bin")
                                                .setLastDigits("masked")))
                                        .setClientInfo(new ClientInfo()))
                                .setContactInfo(new ContactInfo())))
                .setCost(new Cash()
                        .setAmount(123L)
                        .setCurrency(new CurrencyRef()
                                .setSymbolicCode("RUB")))
                .setFlow(new InvoicePaymentFlow());

        return InvoiceChange.invoice_payment_change(
                new InvoicePaymentChange()
                        .setId("1")
                        .setPayload(
                                InvoicePaymentChangePayload.invoice_payment_started(
                                        new InvoicePaymentStarted().setPayment(payment)
                                )));
    }


    private InvoiceChange getInvoicePaymentChangeStatus() {
        return InvoiceChange.invoice_payment_change(
                new InvoicePaymentChange()
                        .setId("1")
                        .setPayload(InvoicePaymentChangePayload.invoice_payment_status_changed(
                                new InvoicePaymentStatusChanged()
                                        .setStatus(InvoicePaymentStatus.processed(new InvoicePaymentProcessed())))));
    }

    @NotNull
    private InvoiceChange getInvoiceCreated() {
        InvoiceChange ic = new InvoiceChange();
        InvoiceCreated invoiceCreated = new InvoiceCreated();
        Invoice invoice = new Invoice();
        invoiceCreated.setInvoice(
                invoice.setId("invoiceId")
                        .setOwnerId("partyId")
                        .setShopId("shopId")
                        .setCreatedAt("2016-03-22T06:12:27Z")
                        .setStatus(InvoiceStatus.unpaid(new InvoiceUnpaid()))
                        .setDetails(new InvoiceDetails()
                                .setProduct("product")
                                .setCart(new InvoiceCart()
                                        .setLines(Arrays.asList(
                                                new InvoiceLine()
                                                        .setQuantity(1)
                                                        .setProduct("product")
                                                        .setPrice(new Cash(1L, new CurrencyRef("RUB")))))))
                        .setDue("2016-03-22T06:12:27Z")
                        .setCost(new Cash()
                                .setAmount(123L)
                                .setCurrency(new CurrencyRef()
                                        .setSymbolicCode("RUB")))
                        .setContext(new Content()));
        ic.setInvoiceCreated(invoiceCreated);
        return ic;
    }
}
