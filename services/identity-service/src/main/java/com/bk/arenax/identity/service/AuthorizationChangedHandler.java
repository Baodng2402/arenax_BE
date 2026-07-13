package com.bk.arenax.identity.service;

import com.bk.arenax.identity.domain.entity.AuthorizationProjection;
import com.bk.arenax.identity.domain.entity.OnboardingProgress;
import com.bk.arenax.identity.messaging.AuthorizationChangedPayload;
import com.bk.arenax.identity.messaging.EventEnvelope;
import com.bk.arenax.identity.repository.AuthorizationProjectionRepository;
import com.bk.arenax.identity.repository.OnboardingProgressRepository;
import java.util.Comparator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthorizationChangedHandler {

    private final AuthorizationProjectionRepository authorizationProjectionRepository;
    private final OnboardingProgressRepository onboardingProgressRepository;
    private final OnboardingActivationService onboardingActivationService;

    public AuthorizationChangedHandler(
            AuthorizationProjectionRepository authorizationProjectionRepository,
            OnboardingProgressRepository onboardingProgressRepository,
            OnboardingActivationService onboardingActivationService) {
        this.authorizationProjectionRepository = authorizationProjectionRepository;
        this.onboardingProgressRepository = onboardingProgressRepository;
        this.onboardingActivationService = onboardingActivationService;
    }

    @Transactional
    public void handle(EventEnvelope<AuthorizationChangedPayload> event) {
        AuthorizationChangedPayload payload = event.payload();
        AuthorizationProjection projection = authorizationProjectionRepository
                .findByUserIdAndAccountId(payload.userId(), payload.accountId())
                .orElseGet(AuthorizationProjection::new);
        projection.setUserId(payload.userId());
        projection.setAccountId(payload.accountId());
        projection.setRoles(payload.roles().stream().sorted().toList());
        projection.setPermissions(payload.permissions().stream().sorted(Comparator.naturalOrder()).toList());
        authorizationProjectionRepository.save(projection);

        OnboardingProgress progress = onboardingProgressRepository.findById(event.correlationId()).orElseThrow();
        progress.setUserId(payload.userId());
        progress.setAccountId(payload.accountId());
        progress.setAuthorizationReady(true);
        onboardingProgressRepository.save(progress);
        onboardingActivationService.activateIfComplete(progress);
    }
}
