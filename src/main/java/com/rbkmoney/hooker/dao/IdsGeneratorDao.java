package com.rbkmoney.hooker.dao;

import com.rbkmoney.hooker.exception.DaoException;

import java.util.List;

public interface IdsGeneratorDao {
    List<Long> get(int size) throws DaoException;
}
