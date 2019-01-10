package com.rbkmoney.hooker.utils;

import com.rbkmoney.hooker.AbstractIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.Assert.*;

public class HashUtilsTest  extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testGetIntHash() {
        Integer javaHash = HashUtils.getIntHash("kek");
        Integer postgresHash = jdbcTemplate.queryForObject("select ('x0'||substr(md5('kek'), 1, 7))::bit(32)::int", Integer.class);
        assertEquals(javaHash, postgresHash);
    }
}