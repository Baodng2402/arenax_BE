package com.bk.arenax.subscription.infrastructure.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.bk.arenax.messaging.EventEnvelope;
import com.bk.arenax.subscription.messaging.PersonalAccountCreatedPayload;
import com.bk.arenax.subscription.service.PersonalAccountCreatedHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class PersonalAccountCreatedEventConsumer {

  private final PersonalAccountCreatedHandler handler;
  private final ObjectMapper objectMapper;

  public PersonalAccountCreatedEventConsumer(
      PersonalAccountCreatedHandler handler, ObjectMapper objectMapper) {
    this.handler = handler;
    this.objectMapper = objectMapper;
  }

  @RabbitListener(queues = RabbitMqConfiguration.ONBOARDING_QUEUE)
  public void onPersonalAccountCreated(@Payload String payload) throws JsonProcessingException {
    EventEnvelope<PersonalAccountCreatedPayload> envelope =
        objectMapper.readValue(
            payload,
            objectMapper
                .getTypeFactory()
                .constructParametricType(EventEnvelope.class, PersonalAccountCreatedPayload.class));
    handler.handle(envelope);
  }
}
