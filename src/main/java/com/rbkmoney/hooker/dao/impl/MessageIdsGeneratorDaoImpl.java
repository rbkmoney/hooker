package com.rbkmoney.hooker.dao.impl;

import com.rbkmoney.hooker.dao.IdsGeneratorDao;
import com.rbkmoney.hooker.exception.DaoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NestedRuntimeException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageIdsGeneratorDaoImpl implements IdsGeneratorDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public List<Long> get(int size) throws DaoException {
        try {
            String sql = "select nextval('hook.seq') from generate_series(1, :size)";
            MapSqlParameterSource parameterSource = new MapSqlParameterSource().addValue("size", size);
            return jdbcTemplate.queryForList(sql, parameterSource, Long.class);
        } catch (NestedRuntimeException e) {
            throw new DaoException(e);
        }
    }
}
