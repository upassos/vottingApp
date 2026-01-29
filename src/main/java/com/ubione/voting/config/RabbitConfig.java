package com.ubione.voting.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String EXCHANGE_VOTING_EVENTS = "voting.events";

    @Bean
    public TopicExchange votingExchange() {
        return new TopicExchange(EXCHANGE_VOTING_EVENTS, true, false);
    }
}
