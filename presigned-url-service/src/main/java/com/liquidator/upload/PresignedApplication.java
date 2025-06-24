package com.liquidator.upload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class PresignedApplication {
    public static void main(String[] args) {
        SpringApplication.run(PresignedApplication.class, args);
    }
}
