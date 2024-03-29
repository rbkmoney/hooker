package com.rbkmoney.hooker.configuration;

import com.rbkmoney.damsel.payment_processing.CustomerManagementSrv;
import com.rbkmoney.damsel.payment_processing.InvoicingSrv;
import com.rbkmoney.hooker.configuration.meta.UserIdentityIdExtensionKit;
import com.rbkmoney.hooker.configuration.meta.UserIdentityRealmExtensionKit;
import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

@Configuration
public class HgConfig {
    @Bean
    public InvoicingSrv.Iface invoicingClient(@Value("${service.invoicing.url}") Resource resource,
                                              @Value("${service.invoicing.networkTimeout}") int networkTimeout)
            throws IOException {
        return new THSpawnClientBuilder()
                .withNetworkTimeout(networkTimeout)
                .withAddress(resource.getURI()).build(InvoicingSrv.Iface.class);
    }

    @Bean
    public CustomerManagementSrv.Iface customerClient(@Value("${service.customer.url}") Resource resource,
                                                      @Value("${service.customer.networkTimeout}") int networkTimeout)
            throws IOException {
        return new THSpawnClientBuilder()
                .withMetaExtensions(List.of(
                        UserIdentityIdExtensionKit.INSTANCE,
                        UserIdentityRealmExtensionKit.INSTANCE))
                .withNetworkTimeout(networkTimeout)
                .withAddress(resource.getURI()).build(CustomerManagementSrv.Iface.class);
    }
}
