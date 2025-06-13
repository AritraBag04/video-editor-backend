package com.liquidator.execute_command;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JobStatusRabbitMQConfig {

    public static final String EXCHANGE_NAME = "job-status-exchange";
    public static final String QUEUE_NAME = "job-status-queue";

    @Bean
    public Queue jobStatusQueue() {
        return new Queue(QUEUE_NAME, true); // durable
    }

    @Bean
    public DirectExchange jobStatusExchange() {
        return new DirectExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Binding jobStatusBinding(Queue jobStatusQueue, DirectExchange jobStatusExchange) {
        return BindingBuilder
                .bind(jobStatusQueue)
                .to(jobStatusExchange)
                .with(""); // empty routing key
    }
}

