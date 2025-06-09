package com.liquidator.execute_command;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
public class ExecuteCommandApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExecuteCommandApplication.class, args);
    }
}
