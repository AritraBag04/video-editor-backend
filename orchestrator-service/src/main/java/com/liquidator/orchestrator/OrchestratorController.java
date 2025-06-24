package com.liquidator.orchestrator;

import com.liquidator.filter_complex.FilterTimelineRequest;
import com.liquidator.input_service.Input;
import com.liquidator.messages.BuildCommandMessage;
import com.liquidator.messages.DownloadFilesMessage;
import com.liquidator.messages.PresignedURLMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/process")
public class OrchestratorController {

    private final RabbitTemplate rabbitTemplate;

    OrchestratorController(RabbitTemplate rabbitTemplate){
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostMapping
    public ResponseEntity<ProcessResponse> export(@RequestBody ProcessRequest request){

        log.info("Request for export {}", request);
        try {
            // Generate unique request ID for tracking
            String requestId = UUID.randomUUID().toString();

            String projectId = request.getProjectId();
            String userEmail = request.getUserEmail();
            // input for input-processing-service
            Input input = request.getInput();
            //filterTimelineRequest for filter-complex-service
            FilterTimelineRequest filter = request.getFilterTimelineRequest();
            BuildCommandMessage buildCommandMessage = new BuildCommandMessage(
                    requestId,
                    input,
                    filter
            );
            DownloadFilesMessage downloadMessage = new DownloadFilesMessage(
                    requestId,
                    userEmail,
                    projectId,
                    input.getVideoTracks(),
                    input.getAudioTracks()
            );
            // Send messages to respective services via RabbitMQ

            rabbitTemplate.convertAndSend(
                    OrchestratorRabbitMQConfig.BUILD_COMMAND_ROUTING_KEY,
                    buildCommandMessage
            );

            rabbitTemplate.convertAndSend(
                    OrchestratorRabbitMQConfig.DOWNLOAD_FILES_ROUTING_KEY,
                    downloadMessage
            );

            // Return response immediately (async processing)
            ProcessResponse response = new ProcessResponse(
                    requestId,
                    "PROCESSING",
                    "Video processing request submitted successfully"
            );

            return ResponseEntity.accepted().body(response);
        } catch (Exception e) {
            log.error("Error processing export request: {}", e.getMessage(), e);

            ProcessResponse errorResponse = new ProcessResponse(
                    null,
                    "ERROR",
                    "Failed to process video export request: " + e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }

    }
}
