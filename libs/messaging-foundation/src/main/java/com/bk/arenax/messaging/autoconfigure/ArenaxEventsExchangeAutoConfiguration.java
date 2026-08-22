package com.bk.arenax.messaging.autoconfigure;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ArenaxEventsExchangeAutoConfiguration {

    public static final String EXCHANGE = "arenax.events";

    @Bean
    TopicExchange arenaxEventsExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }
}