package com.rbkmoney.hooker.converter;

import com.rbkmoney.swag_webhook_events.model.ContactInfo;
import com.rbkmoney.swag_webhook_events.model.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerConverter implements Converter<com.rbkmoney.damsel.payment_processing.Customer, Customer> {

    private final MetadataDeserializer deserializer;

    @Override
    public Customer convert(com.rbkmoney.damsel.payment_processing.Customer source) {
        return new Customer()
                .id(source.getId())
                .shopID(source.getShopId())
                .status(Customer.StatusEnum.fromValue(source.getStatus().getSetField().getFieldName()))
                .contactInfo(new ContactInfo()
                        .email(source.getContactInfo().getEmail())
                        .phoneNumber(source.getContactInfo().getPhoneNumber()))
                .metadata(deserializer.deserialize(source.getMetadata()));
    }
}
