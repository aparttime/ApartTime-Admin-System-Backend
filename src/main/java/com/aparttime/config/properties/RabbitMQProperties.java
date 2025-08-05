package com.aparttime.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rabbitmq")
public record RabbitMQProperties(
    String exchangeName,
    String queueName
) {

}
