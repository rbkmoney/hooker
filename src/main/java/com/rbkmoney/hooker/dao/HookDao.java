package com.rbkmoney.hooker.dao;

import com.rbkmoney.hooker.exception.DaoException;
import com.rbkmoney.hooker.model.Hook;
import com.rbkmoney.hooker.model.PartyMetadata;

import java.util.List;
import java.util.Map;

/**
 * Created by inal on 28.11.2016.
 */
public interface HookDao {
    List<Hook> getPartyHooks(String partyId) throws DaoException;

    PartyMetadata getPartyMetadata(String partyId) throws DaoException;

    int getShopHooksCount(String partyId, String shopId) throws DaoException;

    int getPartyHooksCount(String partyId) throws DaoException;

    Hook getHookById(long id) throws DaoException;

    Hook create(Hook hook) throws DaoException;

    void delete(long id) throws DaoException;

    void updateAvailability(long id, double availability) throws DaoException;
}
