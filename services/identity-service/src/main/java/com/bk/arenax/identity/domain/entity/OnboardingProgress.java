package com.bk.arenax.identity.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "onboarding_progress")
public class OnboardingProgress extends BaseEntity {

    @Id
    @Column(name = "correlation_id", nullable = false)
    private UUID correlationId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "account_id")
    private UUID accountId;

    @Column(name = "authorization_ready", nullable = false)
    private boolean authorizationReady;

    @Column(name = "subscription_ready", nullable = false)
    private boolean subscriptionReady;
}
