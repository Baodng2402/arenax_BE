package com.bk.arenax.competition.domain.entity;

import com.bk.arenax.competition.domain.enums.MatchStatus;
import com.bk.arenax.competition.domain.enums.MatchType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "matches")
public class Match extends BaseEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID sportId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MatchType matchType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MatchStatus status;

    @Column
    private Integer team1Score;

    @Column
    private Integer team2Score;

    @Column
    private Instant finishedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    @PrePersist
    void assignId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null) {
            status = MatchStatus.PENDING;
        }
    }
}
