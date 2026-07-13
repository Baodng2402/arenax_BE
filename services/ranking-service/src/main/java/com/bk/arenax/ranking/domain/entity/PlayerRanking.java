package com.bk.arenax.ranking.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "player_rankings")
public class PlayerRanking extends BaseEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID userId;

    @Column(nullable = false)
    private Integer rating;

    @Column(nullable = false)
    private Integer wins;

    @Column(nullable = false)
    private Integer losses;

    @PrePersist
    void assignId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (rating == null) {
            rating = 1000;
        }
        if (wins == null) {
            wins = 0;
        }
        if (losses == null) {
            losses = 0;
        }
    }
}
