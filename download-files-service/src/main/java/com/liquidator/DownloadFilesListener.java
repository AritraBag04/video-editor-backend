package com.liquidator;

import com.liquidator.messages.DownloadFilesMessage;
import com.liquidator.messages.JobStatusMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.File;
import java.io.IOException;

@Slf4j
@Component
public class DownloadFilesListener {
    private final RabbitTemplate rabbitTemplate;
    private final RedisTemplate redisTemplate;

    @Autowired
    public DownloadFilesListener(RabbitTemplate rabbitTemplate, RedisTemplate<String, Object> redisTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.redisTemplate = redisTemplate;
    }

    @RabbitListener(queues = DownloadFilesRabbitMQConfig.QUEUE_NAME)
    public void downloadVideos(DownloadFilesMessage message) throws IOException {
        log.info("Received message - {}", message);

        String requestId = message.getRequestid();
        String email = message.getUserId();

        String path = email +"/"+ message.getProjectId();
        log.info("Downloading files for user: {}, project: {}", email, message.getProjectId());
        log.info("Path: {}", path);
        String bucketName = "my-video-editor-app-bucket";
        int expectedFiles = message.getVideoTracks() + message.getAudioTracks();

        log.info("Started polling Redis for uploaded files count for requestId: {}", requestId);
        String redisKey = "job:" + path;
        Long uploadedFiles = 0L;
        int attempt = 0;
        while (uploadedFiles < expectedFiles && attempt < 30) {
            log.info("Uploaded files so far: {}", uploadedFiles);
            log.info("Waiting for more files to be uploaded for requestId: {}", requestId);
            Object value = redisTemplate.opsForHash().get(redisKey, "filesUploaded");
            uploadedFiles = (value instanceof Long) ? (Long) value : (value != null ? Long.parseLong(value.toString()) : 0L);
            try {
                Thread.sleep(2000); // Wait before checking again
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("For some reason, the thread was interrupted while waiting for files to be uploaded");
                throw new IOException("Interrupted while waiting for files to be uploaded", e);
            }
            attempt++;
        }

        S3Client s3Client = S3Client.builder()
                .region(Region.AP_SOUTH_1)
                .build();

        ListObjectsV2Request listReq = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(path)
                .build();

        ListObjectsV2Response listRes = s3Client.listObjectsV2(listReq);

        log.info("Files to download: {}", listRes.contents().size());
        log.info("Started downloading files for requestId: {}", requestId);
        for (S3Object s3Object : listRes.contents()) {
            String key = s3Object.key();
            log.info("Download - {}", key);
            String outputPath = "/app/downloads/" + key.replace(path, "");
            File outputFile = new File(outputPath);

            // If the file exists, delete it
            if (outputFile.exists()) {
                boolean deleted = outputFile.delete();
                if (!deleted) {
                    log.info("Failed to delete existing file: {}", outputFile.getAbsolutePath());
                    throw new IOException("Failed to delete existing file: " + outputFile.getAbsolutePath());
                }
            }
            s3Client.getObject(GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build(),
                    ResponseTransformer.toFile(outputFile)
            );
        }
        log.info("Sending job-status message to job-status-exchange for requestId: {}", requestId);
        rabbitTemplate.convertAndSend("job-status-exchange", "", new JobStatusMessage(requestId, "files-ready", null));
    }
}
