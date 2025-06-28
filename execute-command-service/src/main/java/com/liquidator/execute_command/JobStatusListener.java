package com.liquidator.execute_command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liquidator.Project;
import com.liquidator.messages.JobStatusMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.amqp.core.Message;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class JobStatusListener {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RestTemplate restTemplate;

    @Autowired
    public JobStatusListener(RedisTemplate<String, Object> redisTemplate, RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
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

    public void handleStatus(JobStatusMessage message) throws IOException {
        String key = "job:" + message.getRequestId();

        BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(key);

        if ("files-ready".equals(message.getStatus())) {
            log.info("Files are ready for request ID: {}", message.getRequestId());
            ops.put("filesReady", true);
        }

        if ("command-ready".equals(message.getStatus())) {
            log.info("Received 'command-ready' for requestId {}", message.getRequestId());
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

            String presignedUrl;
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

                log.info("Now uploading the output file to S3");

                ResponseEntity<String> uploadPresignedUrl =
                        restTemplate.getForEntity(
                                "http://PRESIGNED-URL/api/v1/presigned-urls/upload?requestId=" + message.getRequestId(),
                                String.class
                        );

                presignedUrl = uploadPresignedUrl.getBody();
                log.info("Presigned URL for upload: {}", presignedUrl);
                uploadFileToPresignedUrl(presignedUrl, new File("/app/downloads/output.mp4"));
                log.info("File uploaded successfully to S3");

                ResponseEntity<String> downloadPresignedUrl = restTemplate.getForEntity(
                        "http://PRESIGNED-URL/api/v1/presigned-urls/download?requestId=" + message.getRequestId() + "&fileName=output.mp4",
                        String.class
                );

                presignedUrl = downloadPresignedUrl.getBody();
                log.info("Presigned URL for download: {}", presignedUrl);
                Project project = new Project(message.getRequestId(), presignedUrl);
                log.info("Creating project with request ID: {}", project.getRequestId());

                ResponseEntity<String> response = restTemplate.postForEntity(
                        "http://PROJECT/api/v1/project",
                        project,
                        String.class
                );

                log.info("Upload completed successfully. Response: {}", response.getBody());

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    void uploadFileToPresignedUrl (String presignedUrl, File file) throws IOException {
        URL url = new URL(presignedUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("PUT"); // use PUT for presigned S3 URLs
        connection.setRequestProperty("Content-Type", Files.probeContentType(file.toPath())); // optional but recommended
        connection.setRequestProperty("Content-Length", String.valueOf(file.length()));        // optional

        try (OutputStream out = connection.getOutputStream();
             FileInputStream in = new FileInputStream(file)) {

            byte[] buffer = new byte[8192];
            int length;
            while ((length = in.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
        }

        int responseCode = connection.getResponseCode();
        System.out.println("Upload response code: " + responseCode);
        if (responseCode >= 200 && responseCode < 300) {
            System.out.println("Upload successful");
        } else {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            throw new IOException("Upload failed with HTTP code: " + responseCode);
        }
    }
}



