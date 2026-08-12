package com.bk.arenax.ranking.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfiguration {

    public static final String EXCHANGE = "arenax.events";
    public static final String MATCH_QUEUE = "arenax.ranking.matches";

    @Bean
    TopicExchange arenaxEventsExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    Queue matchQueue() {
        return new Queue(MATCH_QUEUE, true);
    }

    @Bean
    Binding matchBinding(TopicExchange arenaxEventsExchange, Queue matchQueue) {
        return BindingBuilder.bind(matchQueue).to(arenaxEventsExchange).with("competition.match-completed.#");
    }
}