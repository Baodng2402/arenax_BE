package com.bk.arenax.ranking.infrastructure.messaging;

import com.bk.arenax.messaging.EventEnvelope;
import com.bk.arenax.ranking.messaging.MatchCompletedPayload;
import com.bk.arenax.ranking.service.MatchCompletedHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class MatchCompletedEventConsumer {

    private final MatchCompletedHandler handler;
    private final ObjectMapper objectMapper;

    public MatchCompletedEventConsumer(MatchCompletedHandler handler, ObjectMapper objectMapper) {
        this.handler = handler;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = RabbitMqConfiguration.MATCH_QUEUE)
    public void onMatchCompleted(@Payload String payload) throws JsonProcessingException {
        EventEnvelope<MatchCompletedPayload> envelope = objectMapper.readValue(
                payload,
                objectMapper.getTypeFactory().constructParametricType(
                        EventEnvelope.class, MatchCompletedPayload.class));
        handler.handle(envelope);
    }
}