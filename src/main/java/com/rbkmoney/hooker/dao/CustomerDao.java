package com.rbkmoney.hooker.dao;

import com.rbkmoney.hooker.model.CustomerMessage;

import java.util.Collection;
import java.util.List;

public interface CustomerDao extends MessageDao<CustomerMessage>{
    CustomerMessage getAny(String customerId, String type) throws DaoException;
}
