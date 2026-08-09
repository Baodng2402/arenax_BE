package com.bk.arenax.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "outbox_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "event_type", nullable = false, length = 120)
    private String eventType;

    @Column(name = "event_version", nullable = false)
    private int eventVersion;

    @Column(name = "correlation_id", nullable = false)
    private UUID correlationId;

    @Column(nullable = false, length = 80)
    private String producer;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Lob
    @Column(nullable = false)
    private String payload;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public static OutboxEvent create(
            String eventType,
            int eventVersion,
            UUID correlationId,
            String producer,
            Instant occurredAt,
            String payload) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.eventType = eventType;
        outboxEvent.eventVersion = eventVersion;
        outboxEvent.correlationId = correlationId;
        outboxEvent.producer = producer;
        outboxEvent.occurredAt = occurredAt;
        outboxEvent.payload = payload;
        return outboxEvent;
    }
}
