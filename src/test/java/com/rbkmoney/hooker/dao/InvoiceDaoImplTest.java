package com.rbkmoney.hooker.dao;

import com.rbkmoney.damsel.base.Content;
import com.rbkmoney.hooker.AbstractIntegrationTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * Created by inalarsanukaev on 09.04.17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class InvoiceDaoImplTest extends AbstractIntegrationTest {
    @Autowired
    InvoiceDao invoiceDao;
    @Before
    public void setUp() throws Exception {
        InvoiceInfo invoiceInfo = new InvoiceInfo();
        invoiceInfo.setEventId(5555);
        invoiceInfo.setInvoiceId("1234");
        invoiceInfo.setPartyId("56678");
        invoiceInfo.setShopId(123);
        invoiceInfo.setAmount(12235);
        invoiceInfo.setCurrency("RUB");
        invoiceInfo.setCreatedAt("12.12.2008");
        Content metadata = new Content();
        metadata.setType("string");
        metadata.setData("somedata".getBytes());
        invoiceInfo.setMetadata(metadata);
        invoiceDao.add(invoiceInfo);
    }

    @After
    public void tearDown() throws Exception {
        Assert.assertTrue(invoiceDao.delete("1234"));
    }

    @Test
    public void get() throws Exception {
        Assert.assertEquals(invoiceDao.get("1234").getAmount(), 12235);
    }

    @Test
    public void getMaxEventId(){
        Assert.assertEquals(invoiceDao.getMaxEventId().longValue(), 5555);
    }
}
