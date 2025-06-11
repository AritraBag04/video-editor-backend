package com.liquidator;

import com.liquidator.orchestrator.DownloadFilesMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.File;

@Slf4j
@Component
public class DownloadFilesListener {
    @RabbitListener(queues = DownloadFilesRabbitMQConfig.QUEUE_NAME)
    public void downloadVideos(DownloadFilesMessage message){
        log.info("Received message - {}", message);

        String path = message.getUserId() + message.getProjectId();
        String bucketName = "my-video-editor-app-bucket";

        S3Client s3Client = S3Client.builder()
                .region(Region.AP_SOUTH_1)
                .build();

        ListObjectsV2Request listReq = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(path)
                .build();

        ListObjectsV2Response listRes = s3Client.listObjectsV2(listReq);

        for (S3Object s3Object : listRes.contents()) {
            String key = s3Object.key();
            log.info("Download - {}", key);
            s3Client.getObject(GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build(),
                    ResponseTransformer.toFile(new File("/home/aritra/Desktop/" + key.replace(path, "")))
            );
        }
    }
}
