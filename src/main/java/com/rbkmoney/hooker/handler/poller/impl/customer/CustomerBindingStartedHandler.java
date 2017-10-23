package com.rbkmoney.hooker.handler.poller.impl.customer;

import com.rbkmoney.damsel.payment_processing.CustomerChange;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.hooker.model.CustomerMessage;
import com.rbkmoney.hooker.model.EventType;
import com.rbkmoney.swag_webhook_events.ClientInfo;
import com.rbkmoney.swag_webhook_events.CustomerBinding;
import com.rbkmoney.swag_webhook_events.PaymentResource;
import com.rbkmoney.swag_webhook_events.PaymentToolDetails;
import org.springframework.stereotype.Component;

import static com.rbkmoney.hooker.utils.PaymentToolUtils.getPaymentToolDetails;

/**
 * Created by inalarsanukaev on 12.10.17.
 */
@Component
public class CustomerBindingStartedHandler extends NeedReadCustomerEventHandler {
    private Filter filter;

    private EventType eventType = EventType.CUSTOMER_BINDING_STARTED;

    public CustomerBindingStartedHandler() {
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
    protected CustomerMessage getCustomerMessage(String customerId) {
        return customerDao.getAny(customerId, AbstractCustomerEventHandler.CUSTOMER);
    }

    @Override
    protected void modifyMessage(CustomerChange cc, Event event, CustomerMessage message) {
        com.rbkmoney.damsel.payment_processing.CustomerBinding bindingOrigin = cc.getCustomerBindingChanged().getPayload().getStarted().getBinding();
        PaymentResource paymentResource = new PaymentResource()
                .paymentSession(bindingOrigin.getPaymentResource().getPaymentSessionId())
                .clientInfo(new ClientInfo()
                        .ip(bindingOrigin.getPaymentResource().getClientInfo().getIpAddress())
                        .fingerprint(bindingOrigin.getPaymentResource().getClientInfo().getFingerprint()));

        String detailsType;
        String cardNum = null;
        String paymentSystem = null;
        String terminalProvider = null;

        if (bindingOrigin.getPaymentResource().getPaymentTool().isSetBankCard()) {
            detailsType = PaymentToolDetails.DetailsTypeEnum.PAYMENTTOOLDETAILSBANKCARD.getValue();
            cardNum = bindingOrigin.getPaymentResource().getPaymentTool().getBankCard().getMaskedPan();
            paymentSystem = bindingOrigin.getPaymentResource().getPaymentTool().getBankCard().getPaymentSystem().name();
            paymentResource.setPaymentToolToken(bindingOrigin.getPaymentResource().getPaymentTool().getBankCard().getToken());
        } else if (bindingOrigin.getPaymentResource().getPaymentTool().isSetPaymentTerminal()) {
            detailsType = PaymentToolDetails.DetailsTypeEnum.PAYMENTTOOLDETAILSPAYMENTTERMINAL.getValue();
        } else {
            throw new UnsupportedOperationException();
        }
        paymentResource.setPaymentToolDetails(getPaymentToolDetails(detailsType, cardNum, paymentSystem, terminalProvider));
        CustomerBinding binding = new CustomerBinding()
                .id(bindingOrigin.getId())
                .status(CustomerBinding.StatusEnum.fromValue(bindingOrigin.getStatus().getSetField().getFieldName()))
                .paymentResource(paymentResource);
        message.setCustomerBinding(binding);
    }
}
