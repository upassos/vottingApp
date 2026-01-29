package com.ubione.voting.infra.messaging;

import com.ubione.voting.config.RabbitConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix = "voting.messaging", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RabbitVotingEventPublisher implements VotingEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public RabbitVotingEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(String routingKey, Map<String, Object> payload) {
        Map<String, Object> event = Map.of(
                "eventId", UUID.randomUUID().toString(),
                "occurredAt", OffsetDateTime.now().toString(),
                "type", routingKey,
                "payload", payload
        );
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_VOTING_EVENTS, routingKey, event);
    }
}
