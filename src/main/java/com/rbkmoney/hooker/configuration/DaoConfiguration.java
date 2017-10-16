package com.rbkmoney.hooker.configuration;

import com.rbkmoney.hooker.dao.*;
import com.rbkmoney.hooker.dao.impl.*;
import org.jooq.Schema;
import org.jooq.impl.SchemaImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DaoConfiguration {

    @Bean
    @DependsOn("dbInitializer")
    public HookDao webhookDao(NamedParameterJdbcTemplate jdbcTemplate) {
        return new HookDaoImpl(jdbcTemplate);
    }

    @Bean
    @DependsOn("dbInitializer")
    public MessageDao messageDao(DataSource dataSource) {
        return new MessageDaoImpl(dataSource);
    }

    @Bean
    @DependsOn("dbInitializer")
    public CustomerDao customerDao(DataSource dataSource) {
        return new CustomerDaoImpl(dataSource);
    }

    @Bean
    @DependsOn("dbInitializer")
    public TaskDao taskDao(DataSource dataSource) {
        return new TaskDaoImpl(dataSource);
    }

    @Bean
    @DependsOn("dbInitializer")
    public SimpleRetryPolicyDao simpleRetryPolicyDao(DataSource dataSource) {
        return new SimpleRetryPolicyDaoImpl(dataSource);
    }

    @Bean
    public Schema dbSchema() {
        return new SchemaImpl("hook");
    }
}
