package com.bk.arenax.tenant.infrastructure.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.bk.arenax.messaging.EventEnvelope;
import com.bk.arenax.tenant.messaging.UserRegisteredPayload;
import com.bk.arenax.tenant.service.UserRegistrationHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class UserRegisteredEventConsumer {

  private final UserRegistrationHandler handler;
  private final ObjectMapper objectMapper;

  public UserRegisteredEventConsumer(UserRegistrationHandler handler, ObjectMapper objectMapper) {
    this.handler = handler;
    this.objectMapper = objectMapper;
  }

  @RabbitListener(queues = RabbitMqConfiguration.ONBOARDING_QUEUE)
  public void onUserRegistered(@Payload String payload) throws JsonProcessingException {
    EventEnvelope<UserRegisteredPayload> envelope =
        objectMapper.readValue(
            payload,
            objectMapper
                .getTypeFactory()
                .constructParametricType(EventEnvelope.class, UserRegisteredPayload.class));
    handler.handle(envelope);
  }
}
