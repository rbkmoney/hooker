package com.rbkmoney.hooker.service;

import com.rbkmoney.damsel.webhooker.Webhook;
import com.rbkmoney.damsel.webhooker.WebhookManagerSrv;
import com.rbkmoney.damsel.webhooker.WebhookNotFound;
import com.rbkmoney.damsel.webhooker.WebhookParams;
import com.rbkmoney.hooker.dao.DaoException;
import com.rbkmoney.hooker.dao.WebhookDao;
import com.rbkmoney.hooker.model.Hook;
import com.rbkmoney.hooker.utils.HookConverter;
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
        List<Hook> hooks = webhookDao.getPartyWebhooks(s);
        return HookConverter.convert(hooks);
    }

    @Override
    public Webhook get(long id) throws WebhookNotFound, TException {
        Hook hook = webhookDao.getWebhookById(id);
        if (hook == null) {
            throw new WebhookNotFound();
        }
        return HookConverter.convert(hook);
    }

    @Override
    public Webhook create(WebhookParams webhookParams) throws TException {
        Hook hook = webhookDao.save(HookConverter.convert(webhookParams));
        if (hook == null) {
            throw new TException("Webhookparams.EventFilter is empty.");
        }
        return HookConverter.convert(hook);
    }

    @Override
    public void delete(long id) throws WebhookNotFound, TException {
        try {
            if (!webhookDao.delete(id)) {
                throw new WebhookNotFound();
            }
        } catch (DaoException e) {
            throw new TException();
        }
    }
}
