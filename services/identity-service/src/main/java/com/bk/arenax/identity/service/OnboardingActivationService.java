package com.bk.arenax.identity.service;

import com.bk.arenax.identity.domain.entity.OnboardingProgress;
import com.bk.arenax.identity.domain.entity.User;
import com.bk.arenax.identity.domain.enums.UserStatus;
import com.bk.arenax.identity.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class OnboardingActivationService {

    private final UserRepository userRepository;

    public OnboardingActivationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void activateIfComplete(OnboardingProgress progress) {
        if (!progress.isAuthorizationReady() || !progress.isSubscriptionReady() || progress.getAccountId() == null) {
            return;
        }

        User user = userRepository.findById(progress.getUserId()).orElseThrow();
        user.setActiveAccountId(progress.getAccountId());
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }
}
