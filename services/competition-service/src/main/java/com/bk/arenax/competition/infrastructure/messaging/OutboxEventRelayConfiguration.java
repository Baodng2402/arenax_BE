package com.bk.arenax.competition.infrastructure.messaging;

import com.bk.arenax.messaging.OutboxEventRelay;
import com.bk.arenax.messaging.OutboxEventStore;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class OutboxEventRelayConfiguration {

    @Bean
    @ConditionalOnProperty(name = "arenax.messaging.relay.enabled", havingValue = "true", matchIfMissing = true)
    OutboxEventRelay outboxEventRelay(AmqpTemplate amqpTemplate, OutboxEventStore outboxEventStore) {
        return new OutboxEventRelay(amqpTemplate, outboxEventStore);
    }
}