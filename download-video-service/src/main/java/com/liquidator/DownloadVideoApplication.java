package com.liquidator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
public class DownloadVideoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DownloadVideoApplication.class, args);
    }
}
