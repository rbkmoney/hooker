package com.rbkmoney.hooker.handler.poller.impl.customer;

import com.rbkmoney.damsel.payment_processing.CustomerChange;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.swag_webhook_events.CustomerCreated;
import org.springframework.stereotype.Component;

/**
 * Created by inalarsanukaev on 12.10.17.
 */
@Component
public class CustomerCreatedHandler extends AbstractCustomerEventHandler {

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
        com.rbkmoney.damsel.payment_processing.CustomerCreated customerOrigin = cc.getCustomerCreated();
        CustomerCreated customer = new CustomerCreated();
    }
}
