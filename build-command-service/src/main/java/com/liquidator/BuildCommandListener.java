package com.liquidator;

import com.liquidator.messages.BuildCommandMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BuildCommandListener {

    @RabbitListener(queues = BuildCommandRabbitMQConfig.QUEUE_NAME)
    public void buildCommand(BuildCommandMessage message) {
        log.info("Received message: {}", message);
    }
}