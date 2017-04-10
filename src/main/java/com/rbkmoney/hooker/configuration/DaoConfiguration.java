package com.rbkmoney.hooker.configuration;

import com.rbkmoney.hooker.dao.InvoiceDao;
import com.rbkmoney.hooker.dao.InvoiceDaoImpl;
import com.rbkmoney.hooker.dao.WebhookDao;
import com.rbkmoney.hooker.dao.WebhookDaoImpl;
import org.jooq.Schema;
import org.jooq.impl.SchemaImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;

@Configuration
public class DaoConfiguration {

    @Bean
    @DependsOn("dbInitializer")
    public WebhookDao webhookDao(DataSource dataSource) {
        return new WebhookDaoImpl(dataSource);
    }

    @Bean
    @DependsOn("dbInitializer")
    public InvoiceDao invoiceDao(DataSource dataSource) {
        return new InvoiceDaoImpl(dataSource);
    }

    @Bean
    public Schema dbSchema() {
        return new SchemaImpl("hook");
    }
}
