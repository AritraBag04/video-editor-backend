package com.liquidator.execute_command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liquidator.messages.JobStatusMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.amqp.core.Message;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
@Service
public class JobStatusListener {

    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public JobStatusListener(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @RabbitListener(queues = "job-status-queue")
    public void handleRawMessage(Message message) {
        log.info("📨 Headers: {}", message.getMessageProperties().getHeaders());
        log.info("📨 Raw body: {}", new String(message.getBody(), StandardCharsets.UTF_8));

        try {
            String json = new String(message.getBody(), StandardCharsets.UTF_8);
            JobStatusMessage jobStatus = new ObjectMapper().readValue(json, JobStatusMessage.class);
            handleStatus(jobStatus);  // call your original logic
        } catch (Exception e) {
            log.error("❌ Deserialization failed", e);
        }
    }

    public void handleStatus(JobStatusMessage message) {
        String key = "job:" + message.getRequestId();

        BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(key);

        if ("files-ready".equals(message.getStatus())) {
            log.info("Files are ready for request ID: {}", message.getRequestId());
            ops.put("filesReady", true);
        }

        if ("command-ready".equals(message.getStatus())) {
            log.info("✅ Received 'command-ready' for requestId {}", message.getRequestId());
            ops.put("commandReady", true);
            ops.put("command", message.getCommand());
        }

        ops.expire(Duration.ofMinutes(10)); // Auto-expire old jobs

        Boolean filesReady = (Boolean) ops.get("filesReady");
        Boolean commandReady = (Boolean) ops.get("commandReady");

        if (Boolean.TRUE.equals(filesReady) && Boolean.TRUE.equals(commandReady)) {
            String command = (String) ops.get("command");
            log.info("Executing command for {}: {}", message.getRequestId(), command);
            ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", command);
            processBuilder.directory(new File("/app/downloads"));
            processBuilder.redirectErrorStream(true);

            try {
                Process process = processBuilder.start();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                }

                int exitCode = process.waitFor();
                System.out.println("Command exited with code " + exitCode);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
