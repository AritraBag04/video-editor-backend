package com.liquidator.upload;

import com.liquidator.input_service.Input;
import com.liquidator.messages.PresignedURLMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class PresignedURLListener {
    @RabbitListener(queues = PresignedUrlsRabbitMQConfig.QUEUE_NAME)
    public void getPresignedURL(PresignedURLMessage message) {
        String bucketName = "my-video-editor-app-bucket"; // Bucket name to be used

        Input input = message.getInput();

        int videoTracks = input.getVideoTracks();
        int audioTracks = input.getAudioTracks();

        // Logging the received video and audio tracks
        log.info("Received request - video: {}, audio: {}", videoTracks, audioTracks);

        ArrayList<String> preSignedURLs = new ArrayList<String>();

        S3Client client = S3Client.builder()
                .region(Region.AP_SOUTH_1)
                .build();

        Map<String, String> metadata = new HashMap<>();
        metadata.put("uploadedBy", "aritra");

        // Handling exception
        if (videoTracks < 0 || audioTracks < 0) {
            throw new IllegalStateException("Invalid input for audio or video");
        }

        // Creating presigned URLs for Video Tracks
        for(int i = 0 ; i < videoTracks; i++){
            preSignedURLs.add(createPresignedUrl(
                    bucketName,
                    "user_test/project_test/video"+i+".mp4",
                    metadata
            ));
        }

        // Creating presigned URLs for Audio Tracks
        for(int i = 0 ; i < audioTracks; i++){
            preSignedURLs.add(createPresignedUrl(
                    bucketName,
                    "user_test/project_test/audio"+i+".mp3",
                    metadata
            ));
        }

        log.info("The presigned urls are {}", preSignedURLs);
    }

    /* Create a presigned URL to use in a subsequent PUT request */
    public String createPresignedUrl(String bucketName, String keyName, Map<String, String> metadata) {
        try (S3Presigner presigner = S3Presigner.create()) {

            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .metadata(metadata)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))  // The URL expires in 10 minutes.
                    .putObjectRequest(objectRequest)
                    .build();


            PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
            String myURL = presignedRequest.url().toString();
            log.info("Presigned URL to upload a file to: [{}]", myURL);
            log.info("HTTP method: [{}]", presignedRequest.httpRequest().method());

            return presignedRequest.url().toExternalForm();
        }
    }
    @Bean
    public Jackson2JsonMessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }
}
