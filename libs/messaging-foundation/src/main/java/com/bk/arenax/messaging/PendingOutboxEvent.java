package com.bk.arenax.messaging;

import java.time.Instant;

public interface PendingOutboxEvent {

  String getEventType();

  String getPayload();

  void setPublishedAt(Instant publishedAt);
}
