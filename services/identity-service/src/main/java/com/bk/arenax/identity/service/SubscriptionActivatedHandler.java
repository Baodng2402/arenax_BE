package com.bk.arenax.identity.service;

import com.bk.arenax.identity.domain.entity.OnboardingProgress;
import com.bk.arenax.identity.messaging.EventEnvelope;
import com.bk.arenax.identity.messaging.SubscriptionActivatedPayload;
import com.bk.arenax.identity.repository.OnboardingProgressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubscriptionActivatedHandler {

    private final OnboardingProgressRepository onboardingProgressRepository;
    private final OnboardingActivationService onboardingActivationService;

    public SubscriptionActivatedHandler(
            OnboardingProgressRepository onboardingProgressRepository,
            OnboardingActivationService onboardingActivationService) {
        this.onboardingProgressRepository = onboardingProgressRepository;
        this.onboardingActivationService = onboardingActivationService;
    }

    @Transactional
    public void handle(EventEnvelope<SubscriptionActivatedPayload> event) {
        OnboardingProgress progress = onboardingProgressRepository.findById(event.correlationId()).orElseThrow();
        progress.setAccountId(event.payload().accountId());
        progress.setSubscriptionReady(true);
        onboardingProgressRepository.save(progress);
        onboardingActivationService.activateIfComplete(progress);
    }
}
