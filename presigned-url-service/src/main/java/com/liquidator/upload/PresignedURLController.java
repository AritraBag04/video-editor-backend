package com.liquidator.upload;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.*;

@Slf4j
@RestController
public class PresignedURLController {
    @GetMapping("/api/v1/presigned-urls")
    public PresignedURLResponse getPresignedURL(
        @RequestParam String userEmail,
        @RequestParam int videoTracks,
        @RequestParam int audioTracks
    ) {
        String bucketName = "my-video-editor-app-bucket"; // Bucket name to be used

        // Logging the received video and audio tracks
        log.info("Received request - video: {}, audio: {}", videoTracks, audioTracks);
        log.info("Received request - userEmail: {}", userEmail);

        ArrayList<String> preSignedURLs = new ArrayList<String>();
        UUID projectId = UUID.randomUUID();

        String path = userEmail + "/" + projectId; // Generate a unique project ID

        S3Client client = S3Client.builder()
                .region(Region.AP_SOUTH_1)
                .build();

        Map<String, String> metadata = new HashMap<>();

        // Handling exception
        if (videoTracks < 0 || audioTracks < 0) {
            throw new IllegalStateException("Invalid input for audio or video");
        }

        // Creating presigned URLs for Video Tracks
        for(int i = 0 ; i < videoTracks; i++){
            preSignedURLs.add(createPresignedUrl(
                    bucketName,
                    path+"/video"+i+".mp4",
                    metadata
            ));
        }

        // Creating presigned URLs for Audio Tracks
        for(int i = 0 ; i < audioTracks; i++){
            preSignedURLs.add(createPresignedUrl(
                    bucketName,
                    path+"/audio"+i+".mp3",
                    metadata
            ));
        }

        log.info("The presigned urls are {}", preSignedURLs);
        return new PresignedURLResponse(
                projectId,
                preSignedURLs
        );
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
