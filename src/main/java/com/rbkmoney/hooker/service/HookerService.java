package com.rbkmoney.hooker.service;

import com.rbkmoney.damsel.webhooker.Webhook;
import com.rbkmoney.damsel.webhooker.WebhookManagerSrv;
import com.rbkmoney.damsel.webhooker.WebhookNotFound;
import com.rbkmoney.damsel.webhooker.WebhookParams;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.dao.WebhookDao;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by inalarsanukaev on 06.04.17.
 */
@Service
public class HookerService implements WebhookManagerSrv.Iface {
    @Autowired
    WebhookDao webhookDao;

    @Override
    public List<Webhook> getList(String s) throws TException {
        return webhookDao.getPartyWebhooks(s);
    }

    @Override
    public Webhook get(String s) throws WebhookNotFound, TException {
        return webhookDao.getWebhookById(s);
    }

    @Override
    public Webhook create(WebhookParams webhookParams) throws TException {
        return webhookDao.addWebhook(webhookParams);
    }

    @Override
    public void delete(String s) throws WebhookNotFound, TException {
        try {
            webhookDao.delete(s);
        } catch (DaoException e) {
            throw new WebhookNotFound();
        }
    }
}
