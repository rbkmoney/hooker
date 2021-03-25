package com.rbkmoney.hooker.handler.poller.customer;

import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.hooker.dao.impl.CustomerDaoImpl;
import com.rbkmoney.hooker.dao.impl.CustomerQueueDao;
import com.rbkmoney.hooker.dao.impl.CustomerTaskDao;
import com.rbkmoney.hooker.model.CustomerMessageEnum;
import com.rbkmoney.hooker.model.EventType;
import org.springframework.stereotype.Component;

/**
 * Created by inalarsanukaev on 12.10.17.
 */
@Component
public class CustomerBindingFailedHandler extends NeedReadCustomerEventHandler {

    private EventType eventType = EventType.CUSTOMER_BINDING_FAILED;

    private Filter filter =
            new PathConditionFilter(new PathConditionRule(eventType.getThriftPath(), new IsNullCondition().not()));

    public CustomerBindingFailedHandler(CustomerDaoImpl customerDao,
                                        CustomerQueueDao customerQueueDao,
                                        CustomerTaskDao customerTaskDao) {
        super(customerDao, customerQueueDao, customerTaskDao);
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
}
