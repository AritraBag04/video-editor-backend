package com.liquidator;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BuildCommandRabbitMQConfig {
    public static final String QUEUE_NAME = "video.processing.build.command.queue";
    public static final String JOB_STATUS_QUEUE = "job-status-queue";
    public static final String ROUTING_KEY = "video.processing.build.command";

    @Bean
    public Queue buildCommandQueue(){
        return new Queue(QUEUE_NAME, false);
    }

//    @Bean
//    public Queue jobStatusQueue() {
//        return new Queue(JOB_STATUS_QUEUE, true); // true = durable
//    }

    @Bean
    public Binding buildCommandBinding(TopicExchange videoProcessingExchange) {
        return BindingBuilder.bind(buildCommandQueue())
                .to(videoProcessingExchange)
                .with(ROUTING_KEY);
    }
}
