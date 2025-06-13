package com.liquidator;

import com.liquidator.filter_complex.FilterResponse;
import com.liquidator.filter_complex.FilterTimelineRequest;
import com.liquidator.input_service.CommandResponse;
import com.liquidator.input_service.Input;
import com.liquidator.messages.BuildCommandMessage;
import com.liquidator.messages.JobStatusMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class BuildCommandListener {
    private final RestTemplate restTemplate;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public BuildCommandListener(RestTemplate restTemplate, RabbitTemplate rabbitTemplate) {
        this.restTemplate = restTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = BuildCommandRabbitMQConfig.QUEUE_NAME)
    public void buildCommand(BuildCommandMessage message) {
        log.info("Received message: {}", message);
        String requestId = message.getRequestid();
        Input input = message.getInput();
        FilterTimelineRequest filter = message.getFilter();
        log.info("Calling input-processing and filter-complex services...");
        CommandResponse commandResponse = restTemplate.postForEntity(
                "http://INPUT-PROCESSING/api/v1/input",
                input,
                CommandResponse.class
        ).getBody();
        FilterResponse filterResponse = restTemplate.postForEntity(
                "http://FILTER-COMPLEX/api/v1/filter-complex",
                filter,
                FilterResponse.class
        ).getBody();

        String command = "ffmpeg "+commandResponse.command()+" -filter_complex \""+filterResponse.filterCommand()+"\" -map \"[outv]\" -map \"[outa]\" -c:v ffv1 -c:a flac output.mkv";
        log.info("Command built {}",command);

        rabbitTemplate.convertAndSend("job-status-exchange", "", new JobStatusMessage(requestId, "command-ready", command));
    }
}