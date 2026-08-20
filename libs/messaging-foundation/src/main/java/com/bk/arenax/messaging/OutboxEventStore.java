package com.bk.arenax.messaging;

import java.util.List;

public interface OutboxEventStore {

    List<? extends PendingOutboxEvent> findPending();

    PendingOutboxEvent save(PendingOutboxEvent event);
}