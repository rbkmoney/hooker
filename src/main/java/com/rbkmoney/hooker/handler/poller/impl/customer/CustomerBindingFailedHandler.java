package com.rbkmoney.hooker.handler.poller.impl.customer;

import com.rbkmoney.damsel.domain.ExternalFailure;
import com.rbkmoney.damsel.domain.OperationFailure;
import com.rbkmoney.damsel.payment_processing.CustomerChange;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.hooker.model.CustomerMessage;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.swag_webhook_events.CustomerBindingError;
import org.springframework.stereotype.Component;

/**
 * Created by inalarsanukaev on 12.10.17.
 */
@Component
public class CustomerBindingFailedHandler extends NeedReadCustomerEventHandler {
    private Filter filter;

    private EventType eventType = EventType.CUSTOMER_BINDING_FAILED;

    public CustomerBindingFailedHandler() {
        filter = new PathConditionFilter(new PathConditionRule(eventType.getThriftFilterPathCoditionRule(), new IsNullCondition().not()));
    }
    @Override
    public Filter getFilter() {
        return filter;
    }

    @Override
    protected String getMessageType() {
        return AbstractCustomerEventHandler.BINDING;
    }

    @Override
    protected EventType getEventType() {
        return eventType;
    }

    @Override
    protected void modifyMessage(CustomerChange cc, Event event, CustomerMessage message) {
        OperationFailure failure = cc.getCustomerBindingChanged().getPayload().getStatusChanged().getStatus().getFailed().getFailure();
        String errCode = null;
        String errMess = null;
        if (failure.isSetExternalFailure()) {
            ExternalFailure external = failure.getExternalFailure();
            errCode = external.getCode();
            errMess = external.getDescription();
        } else if (failure.isSetOperationTimeout()) {
            errCode = "408";
            errMess = "Operation timeout";
        }
        message.getCustomerBinding().setError(new CustomerBindingError()
                .code(errCode)
                .message(errMess));
    }
}
