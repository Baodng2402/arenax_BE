package com.bk.arenax.identity.service;

import com.bk.arenax.identity.domain.entity.OnboardingProgress;
import com.bk.arenax.identity.messaging.DefaultRoleGrantedPayload;
import com.bk.arenax.identity.messaging.EventEnvelope;
import com.bk.arenax.identity.repository.OnboardingProgressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultRoleGrantedHandler {

    private final OnboardingProgressRepository onboardingProgressRepository;
    private final OnboardingActivationService onboardingActivationService;

    public DefaultRoleGrantedHandler(
            OnboardingProgressRepository onboardingProgressRepository,
            OnboardingActivationService onboardingActivationService) {
        this.onboardingProgressRepository = onboardingProgressRepository;
        this.onboardingActivationService = onboardingActivationService;
    }

    @Transactional
    public void handle(EventEnvelope<DefaultRoleGrantedPayload> event) {
        OnboardingProgress progress = onboardingProgressRepository.findById(event.correlationId()).orElseThrow();
        progress.setAccountId(event.payload().accountId());
        onboardingProgressRepository.save(progress);
        onboardingActivationService.activateIfComplete(progress);
    }
}
