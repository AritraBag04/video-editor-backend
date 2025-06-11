package com.liquidator;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableRabbit
@SpringBootApplication
public class BuildCommandApplication {
    public static void main(String[] args) {
        SpringApplication.run(BuildCommandApplication.class, args);
    }
}
