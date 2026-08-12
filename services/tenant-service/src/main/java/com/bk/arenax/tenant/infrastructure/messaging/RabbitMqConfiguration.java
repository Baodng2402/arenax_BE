package com.bk.arenax.tenant.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfiguration {

    public static final String EXCHANGE = "arenax.events";
    public static final String ONBOARDING_QUEUE = "arenax.tenant.onboarding";

    @Bean
    TopicExchange arenaxEventsExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    Queue onboardingQueue() {
        return new Queue(ONBOARDING_QUEUE, true);
    }

    @Bean
    Binding onboardingBinding(TopicExchange arenaxEventsExchange, Queue onboardingQueue) {
        return BindingBuilder.bind(onboardingQueue).to(arenaxEventsExchange).with("identity.user.registered.#");
    }
}