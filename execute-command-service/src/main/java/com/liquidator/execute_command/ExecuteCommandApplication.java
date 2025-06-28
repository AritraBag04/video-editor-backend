package com.liquidator.execute_command;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        exclude = {org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class}
)
public class ExecuteCommandApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExecuteCommandApplication.class, args);
    }
}
