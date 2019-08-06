package com.rbkmoney.hooker.handler.poller.impl.customer;

import com.rbkmoney.damsel.payment_processing.CustomerChange;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.hooker.dao.impl.CustomerDaoImpl;
import com.rbkmoney.hooker.model.CustomerMessage;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.swag_webhook_events.model.ClientInfo;
import com.rbkmoney.swag_webhook_events.model.CustomerBinding;
import com.rbkmoney.swag_webhook_events.model.PaymentResource;
import org.springframework.stereotype.Component;

import static com.rbkmoney.hooker.utils.PaymentToolUtils.getPaymentToolDetails;

/**
 * Created by inalarsanukaev on 12.10.17.
 */
@Component
public class CustomerBindingStartedHandler extends NeedReadCustomerEventHandler {

    private EventType eventType = EventType.CUSTOMER_BINDING_STARTED;

    private Filter filter = new PathConditionFilter(new PathConditionRule(eventType.getThriftFilterPathCoditionRule(), new IsNullCondition().not()));

    public CustomerBindingStartedHandler(CustomerDaoImpl customerDao) {
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
    protected CustomerMessage getCustomerMessage(String customerId) {
        return customerDao.getAny(customerId, AbstractCustomerEventHandler.CUSTOMER);
    }

    @Override
    protected void modifyMessage(CustomerChange cc, CustomerMessage message) {
        com.rbkmoney.damsel.payment_processing.CustomerBinding bindingOrigin = cc.getCustomerBindingChanged().getPayload().getStarted().getBinding();
        PaymentResource paymentResource = new PaymentResource()
                .paymentSession(bindingOrigin.getPaymentResource().getPaymentSessionId())
                .clientInfo(new ClientInfo()
                        .ip(bindingOrigin.getPaymentResource().getClientInfo().getIpAddress())
                        .fingerprint(bindingOrigin.getPaymentResource().getClientInfo().getFingerprint()));

        paymentResource.setPaymentToolDetails(getPaymentToolDetails(bindingOrigin.getPaymentResource().getPaymentTool()));
        CustomerBinding binding = new CustomerBinding()
                .id(bindingOrigin.getId())
                .paymentResource(paymentResource);
        binding.status(CustomerBinding.StatusEnum.fromValue(bindingOrigin.getStatus().getSetField().getFieldName()));
        message.setCustomerBinding(binding);
    }
}
