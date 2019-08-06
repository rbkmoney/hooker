package com.rbkmoney.hooker.handler.poller.impl.customer;

import com.rbkmoney.damsel.payment_processing.CustomerChange;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.hooker.dao.impl.CustomerDaoImpl;
import com.rbkmoney.hooker.model.CustomerMessage;
import com.rbkmoney.hooker.model.EventType;
import org.springframework.stereotype.Component;

/**
 * Created by inalarsanukaev on 12.10.17.
 */
@Component
public class CustomerDeletedHandler extends NeedReadCustomerEventHandler {

    private EventType eventType = EventType.CUSTOMER_DELETED;

    private Filter filter = new PathConditionFilter(new PathConditionRule(eventType.getThriftFilterPathCoditionRule(), new IsNullCondition().not()));

    public CustomerDeletedHandler(CustomerDaoImpl customerDao) {
        super(customerDao);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    @Override
    protected String getMessageType() {
        return AbstractCustomerEventHandler.CUSTOMER;
    }

    @Override
    protected EventType getEventType() {
        return eventType;
    }

    @Override
    protected void modifyMessage(CustomerChange cc, CustomerMessage message) {
    }
}
