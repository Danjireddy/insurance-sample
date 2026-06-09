package com.insurance.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class InsuranceSampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(InsuranceSampleApplication.class, args);
    }
}
