package com.rbkmoney.hooker.handler.poller.impl.customer;

import com.rbkmoney.damsel.payment_processing.CustomerChange;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.hooker.dao.CustomerDao;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.dao.impl.CustomerQueueDao;
import com.rbkmoney.hooker.dao.impl.CustomerTaskDao;
import com.rbkmoney.hooker.model.CustomerMessage;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.utils.CustomerUtils;
import com.rbkmoney.swag_webhook_events.ContactInfo;
import com.rbkmoney.swag_webhook_events.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by inalarsanukaev on 12.10.17.
 */
@Component
public class CustomerCreatedHandler extends AbstractCustomerEventHandler {

    @Autowired
    CustomerDao customerDao;

    @Autowired
    CustomerQueueDao queueDao;

    @Autowired
    CustomerTaskDao taskDao;

    private Filter filter;

    private EventType eventType = EventType.CUSTOMER_CREATED;

    public CustomerCreatedHandler() {
        filter = new PathConditionFilter(new PathConditionRule(eventType.getThriftFilterPathCoditionRule(), new IsNullCondition().not()));
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    @Override
    @Transactional
    protected void saveEvent(CustomerChange cc, Event event) throws DaoException {
        com.rbkmoney.damsel.payment_processing.CustomerCreated customerCreatedOrigin = cc.getCustomerCreated();
        CustomerMessage customerMessage = new CustomerMessage();
        customerMessage.setEventId(event.getId());
        customerMessage.setOccuredAt(event.getCreatedAt());
        customerMessage.setType(CUSTOMER);
        customerMessage.setPartyId(customerCreatedOrigin.getOwnerId());
        customerMessage.setEventType(eventType);
        Customer customer = new Customer()
                .id(customerCreatedOrigin.getCustomerId())
                .shopID(customerCreatedOrigin.getShopId())
                .status(Customer.StatusEnum.fromValue("unready"))
                .contactInfo(new ContactInfo()
                        .email(customerCreatedOrigin.getContactInfo().getEmail())
                        .phoneNumber(customerCreatedOrigin.getContactInfo().getPhoneNumber()))
                .metadata(new CustomerUtils().getResult(customerCreatedOrigin.getMetadata()));
        customerMessage.setCustomer(customer);
        customerDao.createEvent(customerMessage);
        queueDao.createWithPolicy(customerMessage.getId());
        taskDao.create(customerMessage.getId());
    }
}
