package com.rbkmoney.hooker.handler.poller.impl.customer;

import com.rbkmoney.damsel.domain.Failure;
import com.rbkmoney.damsel.domain.OperationFailure;
import com.rbkmoney.damsel.payment_processing.CustomerChange;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.hooker.dao.impl.CustomerDaoImpl;
import com.rbkmoney.hooker.model.CustomerMessage;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.swag_webhook_events.model.CustomerBindingError;
import org.springframework.stereotype.Component;

/**
 * Created by inalarsanukaev on 12.10.17.
 */
@Component
public class CustomerBindingFailedHandler extends NeedReadCustomerEventHandler {

    private EventType eventType = EventType.CUSTOMER_BINDING_FAILED;

    private Filter filter = new PathConditionFilter(new PathConditionRule(eventType.getThriftFilterPathCoditionRule(), new IsNullCondition().not()));

    public CustomerBindingFailedHandler(CustomerDaoImpl customerDao) {
        super(customerDao);
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
    protected void modifyMessage(CustomerChange cc, CustomerMessage message) {
        OperationFailure failure = cc.getCustomerBindingChanged().getPayload().getStatusChanged().getStatus().getFailed().getFailure();
        String errCode = null;
        String errMess = null;
        if (failure.isSetFailure()) {
            Failure external = failure.getFailure();
            errCode = external.getCode();
            errMess = external.getReason();
        } else if (failure.isSetOperationTimeout()) {
            errCode = "408";
            errMess = "Operation timeout";
        }
        message.getCustomerBinding().setError(new CustomerBindingError()
                .code(errCode)
                .message(errMess));
    }
}
