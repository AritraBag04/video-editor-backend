package com.liquidator.input_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class InputProcessingApplication {
    public static void main(String[] args) {
        SpringApplication.run(InputProcessingApplication.class, args);
    }
}
