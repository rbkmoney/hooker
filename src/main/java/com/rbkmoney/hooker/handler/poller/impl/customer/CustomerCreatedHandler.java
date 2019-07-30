package com.rbkmoney.hooker.handler.poller.impl.customer;

import com.rbkmoney.damsel.payment_processing.CustomerChange;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.hooker.dao.CustomerDao;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.model.CustomerMessage;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.hooker.utils.CustomerUtils;
import com.rbkmoney.swag_webhook_events.model.ContactInfo;
import com.rbkmoney.swag_webhook_events.model.Customer;
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
    protected void saveEvent(CustomerChange cc, Long eventId, String eventCreatedAt, String sourceId, Long sequenceId, Integer changeId) throws DaoException {
        com.rbkmoney.damsel.payment_processing.CustomerCreated customerCreatedOrigin = cc.getCustomerCreated();
        CustomerMessage customerMessage = new CustomerMessage();
        customerMessage.setEventId(eventId);
        customerMessage.setOccuredAt(eventCreatedAt);
        customerMessage.setSequenceId(sequenceId);
        customerMessage.setChangeId(changeId);
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
        customerDao.create(customerMessage);
    }
}
