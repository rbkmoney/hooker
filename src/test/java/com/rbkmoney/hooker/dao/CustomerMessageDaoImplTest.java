package com.rbkmoney.hooker.dao;

import com.rbkmoney.hooker.AbstractIntegrationTest;
import com.rbkmoney.hooker.handler.poller.impl.customer.AbstractCustomerEventHandler;
import com.rbkmoney.hooker.model.CustomerMessage;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.swag_webhook_events.Customer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static com.rbkmoney.hooker.utils.BuildUtils.buildCustomerMessage;
import static org.junit.Assert.assertEquals;

/**
 * Created by inalarsanukaev on 09.04.17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CustomerMessageDaoImplTest extends AbstractIntegrationTest {
    private static Logger log = LoggerFactory.getLogger(CustomerMessageDaoImplTest.class);

    @Autowired
    CustomerDao messageDao;

    private static boolean messagesCreated = false;

    @Before
    public void setUp() throws Exception {
        if(!messagesCreated){
            messageDao.create(buildCustomerMessage(1L,"1234", EventType.CUSTOMER_CREATED, AbstractCustomerEventHandler.CUSTOMER, "124", "4356", Customer.StatusEnum.READY));
            messagesCreated = true;
        }
    }

    @Test
    public void get() throws Exception {
        CustomerMessage message = messageDao.getAny("124", AbstractCustomerEventHandler.CUSTOMER);
        assertEquals(message.getCustomer().getShopID(), "4356");
    }

    @Test
    public void getMaxEventId() {
        assertEquals(messageDao.getMaxEventId().longValue(), 1L);
    }
}
