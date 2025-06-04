package com.liquidator.filter_complex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class FilterComplexApplication {
    public static void main(String[] args) {
        SpringApplication.run(FilterComplexApplication.class, args);
    }
}
