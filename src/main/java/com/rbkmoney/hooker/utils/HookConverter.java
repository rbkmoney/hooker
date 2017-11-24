package com.rbkmoney.hooker.utils;

import com.rbkmoney.damsel.webhooker.Webhook;
import com.rbkmoney.damsel.webhooker.WebhookParams;
import com.rbkmoney.hooker.model.Hook;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by jeckep on 13.04.17.
 */
public class HookConverter {
    public static Webhook convert(Hook hook){
        return new Webhook(
                hook.getId(),
                hook.getPartyId(),
                EventFilterUtils.getEventFilter(hook.getFilters()),
                hook.getUrl(),
                hook.getPubKey(),
                hook.isEnabled());
    }

    public static Hook convert(Webhook webhook){
        return new Hook(
                webhook.getId(),
                webhook.getPartyId(),
                EventFilterUtils.getTopic(webhook.getEventFilter()),
                EventFilterUtils.getWebhookAdditionalFilter(webhook.getEventFilter()),
                webhook.getUrl(),
                webhook.getPubKey(),
                null,
                webhook.isEnabled(),
                null);
    }

    public static Hook convert(WebhookParams webhookParams){
        Hook hook = new Hook();
        hook.setPartyId(webhookParams.getPartyId());
        hook.setTopic(EventFilterUtils.getTopic(webhookParams.getEventFilter()));
        hook.setUrl(webhookParams.getUrl());
        hook.setFilters(EventFilterUtils.getWebhookAdditionalFilter(webhookParams.getEventFilter()));

        return hook;
    }

    public static List<Webhook> convert(List<Hook> hooks){
        return hooks.stream().map(h -> convert(h)).collect(Collectors.toList());
    }

}
