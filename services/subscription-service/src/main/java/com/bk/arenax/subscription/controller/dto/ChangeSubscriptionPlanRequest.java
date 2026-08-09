package com.bk.arenax.subscription.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangeSubscriptionPlanRequest(@NotBlank String plan) {

    public ChangeSubscriptionPlanRequest {
        plan = plan == null ? null : plan.trim();
    }
}
