package com.rbkmoney.hooker.handler.poller.impl.customer;

import com.rbkmoney.damsel.payment_processing.CustomerChange;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.hooker.dao.CustomerDao;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.model.CustomerMessage;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.utils.CustomerUtils;
import com.rbkmoney.swag_webhook_events.ContactInfo;
import com.rbkmoney.swag_webhook_events.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by inalarsanukaev on 12.10.17.
 */
@Component
public class CustomerCreatedHandler extends AbstractCustomerEventHandler {

    @Autowired
    CustomerDao customerDao;

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
    protected void saveEvent(CustomerChange cc, Event event) throws DaoException {
        com.rbkmoney.damsel.payment_processing.Customer customerOrigin = cc.getCustomerCreated().getCustomer();
        CustomerMessage customerMessage = new CustomerMessage();
        customerMessage.setEventId(event.getId());
        customerMessage.setOccuredAt(event.getCreatedAt());
        customerMessage.setType(CUSTOMER);
        customerMessage.setPartyId(customerOrigin.getOwnerId());
        customerMessage.setEventType(eventType);
        Customer customer = new Customer()
                .id(customerOrigin.getId())
                .shopID(customerOrigin.getShopId())
                .status(Customer.StatusEnum.fromValue(customerOrigin.getStatus().getSetField().getFieldName()))
                .contactInfo(new ContactInfo()
                        .email(customerOrigin.getContactInfo().getEmail())
                        .phoneNumber(customerOrigin.getContactInfo().getPhoneNumber()))
                .metadata(new CustomerUtils().getResult(customerOrigin.getMetadata()));
        customerMessage.setCustomer(customer);
        customerDao.create(customerMessage);
    }
}
