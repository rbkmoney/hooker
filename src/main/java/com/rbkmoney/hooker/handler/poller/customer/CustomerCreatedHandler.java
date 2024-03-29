package com.rbkmoney.hooker.handler.poller.customer;

import com.rbkmoney.damsel.payment_processing.CustomerChange;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.hooker.dao.impl.CustomerDaoImpl;
import com.rbkmoney.hooker.dao.impl.CustomerQueueDao;
import com.rbkmoney.hooker.dao.impl.CustomerTaskDao;
import com.rbkmoney.hooker.exception.DaoException;
import com.rbkmoney.hooker.model.CustomerMessage;
import com.rbkmoney.hooker.model.CustomerMessageEnum;
import com.rbkmoney.hooker.model.EventInfo;
import com.rbkmoney.hooker.model.EventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Created by inalarsanukaev on 12.10.17.
 */
@Component
@RequiredArgsConstructor
public class CustomerCreatedHandler extends AbstractCustomerEventHandler {

    private final CustomerDaoImpl customerDao;
    private final CustomerQueueDao customerQueueDao;
    private final CustomerTaskDao customerTaskDao;
    private EventType eventType = EventType.CUSTOMER_CREATED;
    private Filter filter =
            new PathConditionFilter(new PathConditionRule(eventType.getThriftPath(), new IsNullCondition().not()));

    @Override
    public Filter getFilter() {
        return filter;
    }

    @Override
    protected void saveEvent(CustomerChange cc, EventInfo eventInfo) throws DaoException {
        com.rbkmoney.damsel.payment_processing.CustomerCreated customerCreatedOrigin = cc.getCustomerCreated();
        CustomerMessage customerMessage = new CustomerMessage();
        customerMessage.setEventId(eventInfo.getEventId());
        customerMessage.setEventTime(eventInfo.getEventCreatedAt());
        customerMessage.setSequenceId(eventInfo.getSequenceId());
        customerMessage.setChangeId(eventInfo.getChangeId());
        customerMessage.setType(CustomerMessageEnum.CUSTOMER);
        customerMessage.setPartyId(customerCreatedOrigin.getOwnerId());
        customerMessage.setEventType(eventType);
        customerMessage.setCustomerId(customerCreatedOrigin.getCustomerId());
        customerMessage.setShopId(customerCreatedOrigin.getShopId());
        Long messageId = customerDao.create(customerMessage);
        if (messageId != null) {
            customerMessage.setId(messageId);
            customerQueueDao.createWithPolicy(messageId);
            customerTaskDao.create(messageId);
        }
    }
}
