package com.bk.arenax.identity.service.support;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.bk.arenax.messaging.EventEnvelope;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class IdentityEventSerializer {

  private final ObjectMapper objectMapper;

  public String writePayload(EventEnvelope<?> envelope) {
    try {
      return objectMapper.writeValueAsString(envelope);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Failed to serialize identity event payload", exception);
    }
  }
}
