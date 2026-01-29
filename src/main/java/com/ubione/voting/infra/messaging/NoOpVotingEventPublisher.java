package com.ubione.voting.infra.messaging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "voting.messaging", name = "enabled", havingValue = "false")
public class NoOpVotingEventPublisher implements VotingEventPublisher {
    @Override
    public void publish(String routingKey, Map<String, Object> payload) {
        // no-op (useful for tests/local without RabbitMQ)
    }
}
