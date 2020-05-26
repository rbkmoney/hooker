package com.rbkmoney.hooker.service;

import com.rbkmoney.damsel.base.Content;
import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.domain.Invoice;
import com.rbkmoney.damsel.domain.InvoicePayment;
import com.rbkmoney.damsel.domain.InvoicePaymentPending;
import com.rbkmoney.damsel.payment_processing.*;
import com.rbkmoney.hooker.AbstractIntegrationTest;
import com.rbkmoney.hooker.dao.HookDao;
import com.rbkmoney.hooker.dao.impl.InvoicingMessageDaoImpl;
import com.rbkmoney.hooker.dao.impl.InvoicingQueueDao;
import com.rbkmoney.hooker.dao.impl.InvoicingTaskDao;
import com.rbkmoney.hooker.handler.poller.invoicing.AbstractInvoiceEventMapper;
import com.rbkmoney.hooker.model.*;
import com.rbkmoney.hooker.utils.BuildUtils;
import com.rbkmoney.hooker.utils.KeyUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;

import static org.junit.Assert.*;

public class BatchProcessingTest extends AbstractIntegrationTest {

    @Value("${message.scheduler.limit}")
    private int limit;

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
        List<InvoicingMessage> messages = new ArrayList<>();

        InvoiceChange ic = getInvoiceCreated();
        EventInfo eventInfo = new EventInfo(null, "2016-03-22T06:12:27Z", "invoiceId",1L,1);
        Optional<AbstractInvoiceEventMapper> eventMapperOptional = handlerManager.getHandler(ic);
        assertTrue(eventMapperOptional.isPresent());
        AbstractInvoiceEventMapper invoiceEventMapper = eventMapperOptional.get();
        InvoicingMessage invoiceCreated = invoiceEventMapper.handle(ic, eventInfo, storage);
        assertNotNull(invoiceCreated);
        assertEquals("invoiceId", invoiceCreated.getInvoiceId());
        assertEquals(InvoicingMessageEnum.INVOICE, invoiceCreated.getType());
        storage.put(KeyUtils.key(invoiceCreated), invoiceCreated);
        messages.add(invoiceCreated);

        InvoiceChange icPaymentStarted = getInvoicePaymentStarted();
        EventInfo eventInfoPaymentStarted = new EventInfo(null, "2016-03-22T06:12:27Z", "invoiceId",1L,2);
        Optional<AbstractInvoiceEventMapper> eventMapperPaymentStartedOptional = handlerManager.getHandler(icPaymentStarted);
        assertTrue(eventMapperPaymentStartedOptional.isPresent());
        AbstractInvoiceEventMapper invoicePaymentStartedEventMapper = eventMapperPaymentStartedOptional.get();
        InvoicingMessage paymentStarted = invoicePaymentStartedEventMapper.handle(icPaymentStarted, eventInfoPaymentStarted, storage);
        assertNotNull(paymentStarted);
        assertEquals(InvoicingMessageEnum.PAYMENT, paymentStarted.getType());
        assertEquals("partyId", paymentStarted.getPartyId());
        assertNotEquals(invoiceCreated.getChangeId(), paymentStarted.getChangeId());
        storage.put(KeyUtils.key(paymentStarted), paymentStarted);
        messages.add(paymentStarted);

        assertEquals(2, storage.size());
        assertEquals(2, messages.size());

        InvoiceChange icStatusChanged = getInvoicePaymentChangeStatus();
        EventInfo eventInfoPaymentStatusChanged = new EventInfo(null, "2016-03-22T06:12:27Z", "invoiceId",1L,3);
        Optional<AbstractInvoiceEventMapper> eventMapperPaymentStatusChangedOptional = handlerManager.getHandler(icStatusChanged);
        assertTrue(eventMapperPaymentStatusChangedOptional.isPresent());
        AbstractInvoiceEventMapper invoicePaymentStatusChangedEventMapper = eventMapperPaymentStatusChangedOptional.get();
        InvoicingMessage statusChanged = invoicePaymentStatusChangedEventMapper.handle(icStatusChanged, eventInfoPaymentStatusChanged, storage);
        assertNotNull(statusChanged);
        assertEquals("partyId", statusChanged.getPartyId());
        assertEquals(PaymentStatusEnum.PROCESSED, statusChanged.getPaymentStatus());
        assertNotEquals(statusChanged.getPaymentStatus(), paymentStarted.getPaymentStatus());
        storage.put(KeyUtils.key(statusChanged), statusChanged);
        messages.add(statusChanged);

        assertEquals(2, storage.size());
        assertEquals(3, messages.size());

        // not found message
        InvoiceChange icStatusNFChanged = getInvoicePaymentChangeStatus();
        EventInfo eventInfoPaymentStatusNFChanged = new EventInfo(null, "2016-03-22T06:12:27Z", "not_found",1L,3);
        Optional<AbstractInvoiceEventMapper> eventMapperPaymentStatusNFChangedOptional = handlerManager.getHandler(icStatusNFChanged);
        assertTrue(eventMapperPaymentStatusChangedOptional.isPresent());
        AbstractInvoiceEventMapper invoicePaymentStatusNFChangedEventMapper = eventMapperPaymentStatusNFChangedOptional.get();
        InvoicingMessage statusNFChanged = invoicePaymentStatusNFChangedEventMapper.handle(icStatusNFChanged, eventInfoPaymentStatusNFChanged, storage);
        assertNull(statusNFChanged);

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

        assertEquals(1, taskDao.getScheduled(limit).size());
        assertEquals(1, invoicingQueueDao.getWithPolicies(Collections.singletonList(1L)).size());

        //test duplication
        batchService.process(messages);
        assertEquals(1, invoicingQueueDao.getWithPolicies(Collections.singletonList(1L)).size());
    }

    private InvoiceChange getInvoicePaymentStarted() {
        return InvoiceChange.invoice_payment_change(
                new InvoicePaymentChange()
                        .setId("1")
                        .setPayload(InvoicePaymentChangePayload.invoice_payment_started(
                                new InvoicePaymentStarted()
                                        .setPayment(new InvoicePayment()
                                                .setId("1")
                                                .setCreatedAt("2016-03-22T06:12:27Z")
                                                .setStatus(InvoicePaymentStatus.pending(new InvoicePaymentPending()))
                                                .setPayer(Payer.payment_resource(
                                                        new PaymentResourcePayer()
                                                                .setResource(new DisposablePaymentResource()
                                                                        .setPaymentTool(PaymentTool.bank_card(new BankCard()
                                                                                .setToken("token")
                                                                                .setPaymentSystem(BankCardPaymentSystem.amex)
                                                                                .setBin("bin")
                                                                                .setMaskedPan("masked")))
                                                                        .setClientInfo(new ClientInfo()))
                                                                .setContactInfo(new ContactInfo())))
                                                .setCost(new Cash()
                                                        .setAmount(123L)
                                                        .setCurrency(new CurrencyRef()
                                                                .setSymbolicCode("RUB")))
                                        .setFlow(new InvoicePaymentFlow())))
                        ));
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
