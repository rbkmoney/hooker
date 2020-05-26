package com.rbkmoney.hooker.handler.poller.customer;

import com.rbkmoney.damsel.payment_processing.CustomerChange;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.hooker.dao.impl.CustomerDaoImpl;
import com.rbkmoney.hooker.model.CustomerMessage;
import com.rbkmoney.hooker.model.CustomerMessageEnum;
import com.rbkmoney.hooker.model.EventType;
import org.springframework.stereotype.Component;


/**
 * Created by inalarsanukaev on 12.10.17.
 */
@Component
public class CustomerBindingStartedHandler extends NeedReadCustomerEventHandler {

    private EventType eventType = EventType.CUSTOMER_BINDING_STARTED;

    private Filter filter = new PathConditionFilter(new PathConditionRule(eventType.getThriftPath(), new IsNullCondition().not()));

    public CustomerBindingStartedHandler(CustomerDaoImpl customerDao) {
        super(customerDao);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    @Override
    protected CustomerMessageEnum getMessageType() {
        return CustomerMessageEnum.BINDING;
    }

    @Override
    protected EventType getEventType() {
        return eventType;
    }

    @Override
    protected CustomerMessage getCustomerMessage(String customerId) {
        return customerDao.getAny(customerId, CustomerMessageEnum.CUSTOMER);
    }

    @Override
    protected void modifyMessage(CustomerChange cc, CustomerMessage message) {
        message.setBindingId(cc.getCustomerBindingChanged().getId());
    }
}
