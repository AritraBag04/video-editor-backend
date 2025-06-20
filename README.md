# Video Editor Backend

A microservices-based video editor backend using Spring Boot, FFmpeg, RabbitMQ, Redis, Eureka, and AWS S3.

---

## Technologies

- Java, Spring Boot, Maven
- FFmpeg (video processing)
- RabbitMQ (message queue)
- Redis (caching, job status)
- Eureka (service discovery)
- AWS S3 (file storage)

---

## Architecture Overview

- **API Gateway**: Entry point, authentication, routing
- **Orchestrator**: Coordinates workflow
- **Pre-Signed-URL Service**: Generates S3 upload URLs
- **Build Command Service**: Constructs FFmpeg commands
- **Input Processing Service**: Handles input file processing
- **Filter Complex Service**: Builds FFmpeg filter-complex strings
- **Download Files Service**: Downloads files from S3
- **Execute Command Service**: Runs FFmpeg to produce final video

---

## Workflow

1. User sends POST request to API Gateway.
2. API Gateway authenticates user.
3. Request forwarded to Orchestrator.
4. Orchestrator requests pre-signed S3 URLs; API Gateway returns them to frontend.
5. Orchestrator asynchronously triggers Build Command Service.
6. Build Command Service calls Input Processing and Filter Complex Services to build FFmpeg command.
7. Services update job status in Redis.
8. Frontend notifies Download Files Service (via API Gateway) after upload.
9. Download Files Service downloads files from S3.
10. When files and command are ready, Execute Command Service runs FFmpeg and stores output in S3.

---

---

## Storage

- **Videos/Files**: AWS S3
- **Job Status/Cache**: Redis

---

## Service Discovery

- All services register with Eureka.

---

## Asynchronous Processing

- RabbitMQ for inter-service messaging.

---

## Security

- API Gateway handles authentication and authorization.

---

## Output

- Final video stored in S3, accessible via pre-signed URL.