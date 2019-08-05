package com.rbkmoney.hooker.dao;

import com.rbkmoney.hooker.model.EventType;
import lombok.*;

/**
 * Created by inalarsanukaev on 18.04.17.
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WebhookAdditionalFilter {
    private EventType eventType;
    private String shopId;
    private String invoiceStatus;
    private String invoicePaymentStatus;
    private String invoicePaymentRefundStatus;
}
