package com.rbkmoney.hooker.dao;

import java.util.List;

public interface IdsGeneratorDao {
    List<Long> get(int size) throws DaoException;
}
