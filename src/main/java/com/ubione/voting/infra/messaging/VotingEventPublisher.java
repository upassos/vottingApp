package com.ubione.voting.infra.messaging;

import java.util.Map;

public interface VotingEventPublisher {
    void publish(String routingKey, Map<String, Object> payload);
}
