#!/bin/bash

# === CONFIGURATION ===
EC2_USER=ubuntu
EC2_HOST=<your-ec2-ip>     # <- Replace with your actual EC2 IP
EC2_PATH=./deploy-tars
SERVICES=(
#  auth-service
#  api-gateway-service
#  eureka-server
#  orchestrator-service
  presigned-url-service
#  download-files-service
#  build-command-service
#  input-processing-service
#  filter-complex-service
#  project-service
)

# === STEP 1: Clean project ===
#echo "Cleaning Maven build..."
#mvn clean package || { echo "Maven clean failed"; exit 1; }
#
## === STEP 2: Build Docker images locally with Jib ===
#for service in "${SERVICES[@]}"; do
#  echo "Building image for $service..."
#  mvn compile jib:dockerBuild -pl $service -DskipTests -Dspring.profiles.active=docker || { echo "Failed to build $service"; exit 1; }
#done
##
### === STEP 3: Save images to .tar files ===
##mkdir -p deploy-tars
#for service in "${SERVICES[@]}"; do
#  echo "Saving image $service:latest to tar file..."
#  docker save $service:latest -o deploy-tars/$service.tar || { echo "Failed to save image $service"; exit 1; }
#done
##
#echo "Building image for execute-command-service..."
#cd execute-command-service
#docker build -t execute-command-service .
#cd ..

#echo "Saving image execute-command-service:latest to tar file..."
#docker save execute-command-service:latest -o ./deploy-tars/execute-command-service.tar

# === STEP 4: Copy tar files to EC2 instance ===
echo "Copying tar files to EC2 instance..."
#scp -i ~/.ssh/EC2Tutorial.pem ./deploy-tars/*.tar $EC2_USER@$EC2_HOST:$EC2_PATH || { echo "Failed to copy tar files"; exit 1; }

#scp -i ~/.ssh/EC2Tutorial.pem ./deploy-tars/api-gateway-service.tar ubuntu@$EC2_HOST:./deploy-tars/api-gateway-service.tar
#scp -i ~/.ssh/EC2Tutorial.pem ./deploy-tars/build-command-service.tar ubuntu@$EC2_HOST:./deploy-tars/build-command-service.tar
#scp -i ~/.ssh/EC2Tutorial.pem ./deploy-tars/execute-command-service.tar ubuntu@$EC2_HOST:./deploy-tars/execute-command-service.tar
scp -i ~/.ssh/EC2Tutorial.pem ./deploy-tars/presigned-url-service.tar ubuntu@$EC2_HOST:./deploy-tars/presigned-url-service.tar