package com.liquidator;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DownloadFilesRabbitMQConfig {

    public static final String QUEUE_NAME = "video.processing.download.files.queue";

    @Bean
    public Queue presignedUrlsQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public Binding presignedUrlsBinding(TopicExchange videoProcessingExchange) {
        return BindingBuilder.bind(presignedUrlsQueue())
                .to(videoProcessingExchange)
                .with("video.processing.download.files");
    }

}
