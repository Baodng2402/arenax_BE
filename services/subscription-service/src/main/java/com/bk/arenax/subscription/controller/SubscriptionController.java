package com.bk.arenax.subscription.controller;

import com.bk.arenax.subscription.controller.dto.ChangeSubscriptionPlanRequest;
import com.bk.arenax.subscription.controller.dto.CurrentSubscriptionResponse;
import com.bk.arenax.subscription.infrastructure.security.GatewayUserPrincipal;
import com.bk.arenax.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/current")
    CurrentSubscriptionResponse current(GatewayUserPrincipal principal) {
        return subscriptionService.getCurrent(principal.accountId());
    }

    @PatchMapping("/current/plan")
    CurrentSubscriptionResponse changePlan(
            GatewayUserPrincipal principal,
            @Valid @RequestBody ChangeSubscriptionPlanRequest request) {
        return subscriptionService.changePlan(principal.accountId(), request.plan());
    }

    @PostMapping("/current/cancel")
    CurrentSubscriptionResponse cancel(GatewayUserPrincipal principal) {
        return subscriptionService.cancel(principal.accountId());
    }
}
