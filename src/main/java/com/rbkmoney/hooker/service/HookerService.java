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
        try {
            return webhookDao.getWebhookById(s);
        } catch (DaoException e) {
            throw new WebhookNotFound();
        }
    }

    @Override
    public Webhook create(WebhookParams webhookParams) throws TException {
        Webhook webhook = webhookDao.addWebhook(webhookParams);
        if (webhook == null) {
            throw new TException("Webhookparams.EventFilter is empty.");
        }
        return webhook;
    }

    @Override
    public void delete(String s) throws WebhookNotFound, TException {
        try {
            if (!webhookDao.delete(s)) {
                throw new WebhookNotFound();
            }
        } catch (DaoException e) {
            throw new TException();
        }
    }
}
