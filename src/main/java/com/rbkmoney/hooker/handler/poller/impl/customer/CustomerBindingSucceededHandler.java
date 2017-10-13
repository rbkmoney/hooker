package com.rbkmoney.hooker.handler.poller.impl.customer;

import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.hooker.model.EventType;
import org.springframework.stereotype.Component;

/**
 * Created by inalarsanukaev on 12.10.17.
 */
@Component
public class CustomerBindingSucceededHandler extends NeedReadCustomerEventHandler {
    private Filter filter;

    private EventType eventType = EventType.CUSTOMER_BINDING_SUCCEEDED;

    public CustomerBindingSucceededHandler() {
        filter = new PathConditionFilter(new PathConditionRule(eventType.getThriftFilterPathCoditionRule(), new IsNullCondition().not()));
    }
    @Override
    public Filter getFilter() {
        return filter;
    }
}
