package com.rbkmoney.hooker.configuration;

import com.rbkmoney.hooker.dao.CustomerDao;
import com.rbkmoney.hooker.dao.HookDao;
import com.rbkmoney.hooker.dao.InvoicingMessageDao;
import com.rbkmoney.hooker.dao.SimpleRetryPolicyDao;
import com.rbkmoney.hooker.dao.impl.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DaoConfiguration {

    @Bean
    public HookDao webhookDao(NamedParameterJdbcTemplate jdbcTemplate) {
        return new HookDaoImpl(jdbcTemplate);
    }

    @Bean
    public InvoicingMessageDao messageDao(DataSource dataSource) {
        return new InvoicingMessageDaoImpl(dataSource);
    }

    @Bean
    public CustomerDao customerDao(DataSource dataSource) {
        return new CustomerDaoImpl(dataSource);
    }

    @Bean
    public InvoicingTaskDao invoicingTaskDao(DataSource dataSource) {
        return new InvoicingTaskDao(dataSource);
    }

    @Bean
    public CustomerTaskDao customerTaskDao(DataSource dataSource) {
        return new CustomerTaskDao(dataSource);
    }

    @Bean
    public SimpleRetryPolicyDao simpleRetryPolicyDao(DataSource dataSource) {
        return new SimpleRetryPolicyDaoImpl(dataSource);
    }

    @Bean
    public InvoicingQueueDao invoicingQueueDao(DataSource dataSource) {
        return new InvoicingQueueDao(dataSource);
    }

    @Bean
    public CustomerQueueDao customerQueueDao(DataSource dataSource) {
        return new CustomerQueueDao(dataSource);
    }

}
