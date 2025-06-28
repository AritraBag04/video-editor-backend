package com.liquidator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class S3UploadListener {

    private static final Logger log = LoggerFactory.getLogger(S3UploadListener.class);
    private final SqsClient sqsClient = SqsClient.builder()
            .region(Region.AP_SOUTH_1)
            .build();
    private final String queueUrl = "https://sqs.ap-south-1.amazonaws.com/050752622832/s3-upload-events-queue";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public S3UploadListener(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 1000)
    public void pollS3Events() {
        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(10)
                .build();

        List<Message> messages = sqsClient.receiveMessage(request).messages();
        for (Message message : messages) {
            try {
                log.info("Received message in redis: {}", message.body());
                JsonNode rootNode = objectMapper.readTree(message.body());
                // S3 event messages can contain multiple records
                if (rootNode.has("Records")) {
                    for (JsonNode record : rootNode.get("Records")) {
                        String s3Key = record.at("/s3/object/key").asText();
                        if (s3Key != null && !s3Key.isEmpty()) {
                            s3Key = URLDecoder.decode(s3Key, StandardCharsets.UTF_8);

                            String[] parts = s3Key.split("/");
                            String email = parts[0];
                            String projectId = parts[1];
                            String redisKey = "job:" + email + "/" + projectId;

                            // Increment the count of uploaded files for this job
                            redisTemplate.opsForHash().increment(redisKey, "filesUploaded", 1L);
                            log.info("Incremented file count");
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Failed to process S3 event message", e);
            } finally {
                log.info("Deleting message from SQS queue: {}", message.messageId());
                sqsClient.deleteMessage(DeleteMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .receiptHandle(message.receiptHandle())
                        .build());
            }
        }
    }
}