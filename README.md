# 🎬 Liquidator: Video Editor Backend Platform

The backend for a distributed video editing platform designed to mimic a real-time video editing UI, but with all processing handled asynchronously in the backend. Built using a microservices architecture with Spring Boot, RabbitMQ, Redis, and AWS S3.

---

## 🛠️ Features

- ✂️ **Segment-Based Video Editing**: Accepts timelines with multiple video cuts and rearrangements.
- ☁️ **Presigned URL Generation**: Secure upload of raw footage to S3 buckets.
- 📦 **Distributed Microservices**:
    - File downloader
    - FFmpeg command builder
    - Command executor
- 📶 **Asynchronous Orchestration**: Message-driven coordination using RabbitMQ.
- ⚙️ **Redis for Job Coordination**: Tracks readiness of command and files before execution.
- 🎥 **Backend-Only Simulation**: Allows frontend to show editing UI while actual processing is deferred.

---

## 🧱 Tech Stack

- **Language**: Java (Spring Boot)
- **Queueing**: RabbitMQ (Topic Exchange)
- **Temporary State Store**: Redis
- **Cloud Storage**: AWS S3 (Presigned URLs)
- **Video Processing**: FFmpeg
- **Build Tool**: Maven

---

## 🧩 Microservices Overview

| Service                 | Description |
|------------------------|-------------|
| `orchestrator`         | Publishes job lifecycle messages and request routing |
| `get-presigned-urls`   | Generates presigned upload links for video chunks |
| `download-files`       | Downloads video files from S3 once uploaded |
| `build-command`        | Creates FFmpeg commands based on segment data |
| `execute-command`      | Waits until all assets and commands are ready, then runs the FFmpeg command |
| `job-status-listener`  | Listens to status updates and coordinates final execution via Redis |

---

## ⚙️ Message Flow

1. **Orchestrator** sends a job request.
2. **Get-Presigned-Urls Service** returns signed upload links.
3. Once uploaded, **Download-Files Service** notifies via RabbitMQ.
4. **Build-Command Service** creates the FFmpeg command for the job.
5. **Job-Status Listener** stores progress in Redis.
6. Once all pieces are ready, **Execute-Command Service** runs the job.

---

## 🧪 How to Run

### 🐳 Docker Prerequisites

```bash
docker run -d --name redis -p 6379:6379 redis
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:management
```

# Example Payload
```json
{
  "input": {
        "videoTracks" : 2,
        "audioTracks" : 0
  },
  "filter": {
    "videoTimeline": [
        {
            "videoTrack": 0,
            "start": 0,
            "end": 10
        },
        {
            "videoTrack": 1,
            "start": 5,
            "end": 10
        },
        {
            "videoTrack": 0,
            "start": 10,
            "end": 20
        }
    ],
    "audioTimeline": []
  }
}
```
