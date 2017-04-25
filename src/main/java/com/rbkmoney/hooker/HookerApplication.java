package com.rbkmoney.hooker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;

@ServletComponentScan
@SpringBootApplication(scanBasePackages = {"com.rbkmoney.hooker", "com.rbkmoney.dbinit"})
@EnableCaching
public class HookerApplication {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(HookerApplication.class, args);
    }
}
