package com.rbkmoney.hooker.configuration;

import com.rbkmoney.damsel.fault_detector.FaultDetectorSrv;
import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class FaultDetectorConfig {

    @Bean
    public FaultDetectorSrv.Iface faultDetectorClient(@Value("${service.fault-detector.url}") Resource resource,
                                                      @Value("${service.fault-detector.networkTimeout}") int networkTimeout) throws IOException {

        return new THSpawnClientBuilder()
                .withNetworkTimeout(networkTimeout)
                .withAddress(resource.getURI()).build(FaultDetectorSrv.Iface.class);
    }
}
