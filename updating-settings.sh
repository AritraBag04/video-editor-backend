#!/bin/bash

services=(
  auth-service
  orchestrator-service
  presigned-url-service
  download-files-service
  build-command-service
  input-processing-service
  filter-complex-service
  execute-command-service
)

for service in "${services[@]}"; do
  config_file="$service/src/main/resources/application-docker.yml"

  if [[ -f "$config_file" ]]; then
    echo "Updating RabbitMQ config in $config_file..."

    # Remove old rabbitmq host config (optional but safe)
    sed -i '/spring:\s*$/,/^[^ ]/ { /rabbitmq:/,/^[^ ]/d }' "$config_file"

    # Append new rabbitmq config
    cat <<EOF >> "$config_file"

spring:
  rabbitmq:
    host: rabbitmq
    port: 5672
EOF

  else
    echo "⚠️  $config_file not found, skipping."
  fi
done
