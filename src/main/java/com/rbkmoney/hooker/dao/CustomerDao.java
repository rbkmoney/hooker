package com.rbkmoney.hooker.dao;

import com.rbkmoney.hooker.model.CustomerMessage;

import java.util.Collection;
import java.util.List;

public interface CustomerDao {
    CustomerMessage getAny(String customerId, String type) throws DaoException;
    CustomerMessage create(CustomerMessage customerMessage) throws DaoException;
    Long getMaxEventId();
    List<CustomerMessage> getBy(Collection<Long> messageIds);
}
