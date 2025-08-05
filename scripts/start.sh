#!/usr/bin/env bash

PROJECT_ROOT="/home/ec2-user/app"
JAR_FILE="$PROJECT_ROOT/aparttime-admin.jar"

APP_LOG="$PROJECT_ROOT/application.log"
ERROR_LOG="$PROJECT_ROOT/error.log"
DEPLOY_LOG="$PROJECT_ROOT/deploy.log"

TIME_NOW=$(date +%c)

echo "$TIME_NOW > start.sh 스크립트 실행" >> $DEPLOY_LOG

# AWS
export AWS_DEFAULT_REGION=ap-northeast-2

export DATASOURCE_URL=$(aws ssm get-parameter --name "/aparttime/prod/db-url" --with-decryption --query "Parameter.Value" --output text)
export DATASOURCE_USERNAME=$(aws ssm get-parameter --name "/aparttime/prod/db-username" --with-decryption --query "Parameter.Value" --output text)
export DATASOURCE_PASSWORD=$(aws ssm get-parameter --name "/aparttime/prod/db-password" --with-decryption --query "Parameter.Value" --output text)
export REDIS_HOST=$(aws ssm get-parameter --name "/aparttime/prod/redis-host" --with-decryption --query "Parameter.Value" --output text)
export REDIS_PORT=$(aws ssm get-parameter --name "/aparttime/prod/redis-port" --with-decryption --query "Parameter.Value" --output text)
export KAFKA_BOOTSTRAP_SERVERS=$(aws ssm get-parameter --name "/aparttime/prod/kafka-bootstrap-servers" --with-decryption --query "Parameter.Value" --output text)
export KAFKA_GROUP_ID=$(aws ssm get-parameter --name "/aparttime/prod/kafka-group-id" --with-decryption --query "Parameter.Value" --output text)
export JWT_SECRET=$(aws ssm get-parameter --name "/aparttime/prod/jwt-secret" --with-decryption --query "Parameter.Value" --output text)
export JWT_ACCESS_TOKEN_EXPIRATION=$(aws ssm get-parameter --name "/aparttime/prod/jwt-access-token-expiration" --with-decryption --query "Parameter.Value" --output text)
export JWT_REFRESH_TOKEN_EXPIRATION=$(aws ssm get-parameter --name "/aparttime/prod/jwt-refresh-token-expiration" --with-decryption --query "Parameter.Value" --output text)
export JWT_SECONDARY_TOKEN_EXPIRATION=$(aws ssm get-parameter --name "/aparttime/prod/jwt-secondary-token-expiration" --with-decryption --query "Parameter.Value" --output text)
export RABBITMQ_HOST=$(aws ssm get-parameter --name "/aparttime/prod/rabbitmq/host" --with-decryption --query "Parameter.Value" --output text)
export RABBITMQ_PORT=$(aws ssm get-parameter --name "/aparttime/prod/rabbitmq/port" --with-decryption --query "Parameter.Value" --output text)
export RABBITMQ_USERNAME=$(aws ssm get-parameter --name "/aparttime/prod/rabbitmq/username" --with-decryption --query "Parameter.Value" --output text)
export RABBITMQ_PASSWORD=$(aws ssm get-parameter --name "/aparttime/prod/rabbitmq/password" --with-decryption --query "Parameter.Value" --output text)
export RABBITMQ_VIRTUAL_HOST=$(aws ssm get-parameter --name "/aparttime/prod/rabbitmq/virtual-host" --with-decryption --query "Parameter.Value" --output text)
export RABBITMQ_EXCHANGE_NAME=$(aws ssm get-parameter --name "/aparttime/prod/rabbitmq/exchange-name" --with-decryption --query "Parameter.Value" --output text)
export RABBITMQ_QUEUE_NAME=$(aws ssm get-parameter --name "/aparttime/prod/rabbitmq/queue-name" --with-decryption --query "Parameter.Value" --output text)


# 기존에 실행 중인 앱이 있다면 종료
CURRENT_PID=$(pgrep -f "$JAR_FILE")
if [ -n "$CURRENT_PID" ]; then
  echo "$TIME_NOW > 기존 프로세스 종료: $CURRENT_PID" >> $DEPLOY_LOG
  kill -15 "$CURRENT_PID"
  sleep 5
fi

# jar 실행
echo "$TIME_NOW > $JAR_FILE 실행 시작" >> $DEPLOY_LOG
nohup java -jar -Dspring.profiles.active=release "$JAR_FILE" > "$APP_LOG" 2> "$ERROR_LOG" &

sleep 3
NEW_PID=$(pgrep -f "$JAR_FILE")
echo "$TIME_NOW > 새 프로세스 실행됨: $NEW_PID" >> $DEPLOY_LOG
