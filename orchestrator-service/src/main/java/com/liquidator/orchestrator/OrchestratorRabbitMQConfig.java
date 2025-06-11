package com.liquidator.orchestrator;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class OrchestratorRabbitMQConfig {
    // Exchange - This is what the orchestrator publishes to
    public static final String VIDEO_PROCESSING_EXCHANGE = "video.processing.exchange";

    // Routing Keys - These determine which queues receive the messages
    public static final String PRESIGNED_URLS_ROUTING_KEY = "video.processing.presigned.urls";
    public static final String BUILD_COMMAND_ROUTING_KEY = "video.processing.build.command";
    public static final String DOWNLOAD_FILES_ROUTING_KEY = "video.processing.download.files";

    @Bean
    public TopicExchange videoProcessingExchange() {
        return new TopicExchange(VIDEO_PROCESSING_EXCHANGE, true, false);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate((org.springframework.amqp.rabbit.connection.ConnectionFactory) connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter());

        // Optional: Set default exchange (so you don't have to specify it every time)
        template.setExchange(VIDEO_PROCESSING_EXCHANGE);

        // Optional: Enable publisher confirms for reliability
        template.setMandatory(true);

        return template;
    }
}
