package com.liquidator;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableRabbit
@SpringBootApplication
public class DownloadFilesApplication {
    public static void main(String[] args) {
        SpringApplication.run(DownloadFilesApplication.class, args);
    }
}
