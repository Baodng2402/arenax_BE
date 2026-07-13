package com.bk.arenax.identity.repository;

import com.bk.arenax.identity.domain.entity.OnboardingProgress;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OnboardingProgressRepository extends JpaRepository<OnboardingProgress, UUID> {}
