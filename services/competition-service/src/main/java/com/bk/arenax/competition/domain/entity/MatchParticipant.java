package com.bk.arenax.competition.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "match_participants")
public class MatchParticipant extends BaseEntity {

  @Id private UUID id;

  @Column(nullable = false)
  private UUID matchId;

  @Column(nullable = false)
  private UUID userId;

  @Column(nullable = false)
  private Integer teamNumber;

  @PrePersist
  void assignId() {
    if (id == null) {
      id = UUID.randomUUID();
    }
  }
}
