package com.liquidator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.File;

@Slf4j
@RestController
@RequestMapping("/api/v1/download-videos")
public class DownloadVideoController {
    @GetMapping("/{userId}/{projectId}")
    public void downloadVideos(@PathVariable String userId, @PathVariable String projectId){
        String path = userId +"/"+ projectId;
        String bucketName = "my-video-editor-app-bucket";

        S3Client s3Client = S3Client.builder()
                .region(Region.AP_SOUTH_1)
                .build();

        ListObjectsV2Request listReq = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(path)
                .build();

        ListObjectsV2Response listRes = s3Client.listObjectsV2(listReq);
        System.out.println("Printing stuff before entering the loop");
        System.out.println("The lenght of listRes: "+listRes.contents().size());

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
