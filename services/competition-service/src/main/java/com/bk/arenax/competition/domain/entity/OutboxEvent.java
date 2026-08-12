package com.bk.arenax.competition.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "outbox_events")
public class OutboxEvent extends BaseEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 120)
    private String eventType;

    @Column(nullable = false)
    private Integer eventVersion;

    @Column(nullable = false)
    private UUID correlationId;

    @Column(nullable = false, length = 80)
    private String producer;

    @Column(nullable = false)
    private Instant occurredAt;

    @Column(nullable = false, columnDefinition = "clob")
    private String payload;

    @Column(name = "published_at")
    private Instant publishedAt;

    @PrePersist
    void assignId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
