package com.rbkmoney.hooker.dao;

import com.rbkmoney.hooker.exception.DaoException;
import com.rbkmoney.hooker.model.CustomerMessage;
import com.rbkmoney.hooker.model.CustomerMessageEnum;

public interface CustomerDao extends MessageDao<CustomerMessage>{
    CustomerMessage getAny(String customerId, CustomerMessageEnum type) throws DaoException;
}
