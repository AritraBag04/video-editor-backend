package com.liquidator.upload;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class PresignedUrlsRabbitMQConfig {

    public static final String EXCHANGE_NAME = "video.processing.exchange";
    public static final String ROUTING_KEY = "video.processing.presigned.urls";
    public static final String QUEUE_NAME = "video.processing.presigned.urls.queue";

    @Bean
    public TopicExchange videoProcessingExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue presignedUrlsQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public Binding presignedUrlsBinding(TopicExchange videoProcessingExchange) {
        return BindingBuilder.bind(presignedUrlsQueue())
                .to(videoProcessingExchange)
                .with(ROUTING_KEY);
    }
}