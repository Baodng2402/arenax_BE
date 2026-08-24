package com.bk.arenax.messaging.autoconfigure;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.bk.arenax.messaging.OutboxEventRelay;
import com.bk.arenax.messaging.OutboxEventStore;

@Configuration
@EnableScheduling
@ConditionalOnProperty(
    name = "arenax.messaging.relay.enabled",
    havingValue = "true",
    matchIfMissing = true)
@ConditionalOnBean(OutboxEventStore.class)
public class OutboxRelayAutoConfiguration {

  @Bean
  OutboxEventRelay outboxEventRelay(AmqpTemplate amqpTemplate, OutboxEventStore outboxEventStore) {
    return new OutboxEventRelay(amqpTemplate, outboxEventStore);
  }
}
