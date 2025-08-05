package com.aparttime.config;

import com.aparttime.config.properties.RabbitMQProperties;
import com.aparttime.notification.NotificationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RabbitMQNotificationConfig {

    private final RabbitMQProperties rabbitMQProperties;

    /**
     * FanoutExchange - 모든 Queue로 메시지를 broadcast
     */
    @Bean
    public FanoutExchange notificationExchange() {
        return new FanoutExchange(
            rabbitMQProperties.exchangeName(),
            true,
            false
        );
    }

    /**
     * 단일 Notification Queue
     */
    @Bean
    public Queue notificationQueue() {
        return new Queue(
            rabbitMQProperties.queueName(),
            true,
            false,
            false
        );
    }

    @Bean
    public Binding notificationBinding(
        FanoutExchange notificationExchange,
        Queue notificationQueue
    ) {
        return BindingBuilder
            .bind(notificationQueue)
            .to(notificationExchange);
    }

    @Bean
    public SimpleMessageListenerContainer messageListenerContainer(
        ConnectionFactory connectionFactory,
        Queue notificationQueue,
        NotificationService notificationService,
        ObjectMapper objectMapper
    ) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueues(notificationQueue);

        container.setMessageListener(message -> {
            try {
                String body = new String(message.getBody());
                log.info("[RabbitMQNotificationListener] Received message body: {}", body);

                // TODO: 추후 DTO 도입
                Map<String, Object> payload = objectMapper.readValue(
                    body,
                    new TypeReference<>() {
                    }
                );

                notificationService.handlePayload(payload);
            } catch (Exception e) {
                // TODO: 예외 처리 필요
                log.error("[RabbitMQNotificationListener] Failed to process message", e);
                throw new RuntimeException("Failed to process RabbitMQ message", e);
            }
        });

        return container;
    }

}
