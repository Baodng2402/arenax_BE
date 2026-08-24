package com.bk.arenax.subscription;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.bk.arenax.subscription.domain.entity.OutboxEvent;
import com.bk.arenax.subscription.domain.entity.Subscription;
import com.bk.arenax.subscription.domain.enums.SubscriptionPlan;
import com.bk.arenax.subscription.domain.enums.SubscriptionStatus;
import com.bk.arenax.subscription.repository.OutboxEventRepository;
import com.bk.arenax.subscription.repository.SubscriptionRepository;

@SpringBootTest
@AutoConfigureMockMvc
class SubscriptionControllerIntegrationTests {

  @Autowired private MockMvc mockMvc;

  @Autowired private SubscriptionRepository subscriptionRepository;

  @Autowired private OutboxEventRepository outboxEventRepository;

  @BeforeEach
  void setUp() {
    outboxEventRepository.deleteAll();
    subscriptionRepository.deleteAll();
  }

  @Test
  void currentSubscriptionReturnsPlanStatusAndEntitlementsForCurrentAccount() throws Exception {
    UUID accountId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    seedSubscription(accountId, SubscriptionPlan.FREE, SubscriptionStatus.ACTIVE);

    mockMvc
        .perform(trusted(get("/api/v1/subscriptions/current"), accountId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accountId").value(accountId.toString()))
        .andExpect(jsonPath("$.plan").value("FREE"))
        .andExpect(jsonPath("$.status").value("ACTIVE"))
        .andExpect(jsonPath("$.entitlements[0]").value("ACCOUNT_CORE"));
  }

  @Test
  void changePlanUpdatesSubscriptionAndPublishesOutboxEvent() throws Exception {
    UUID accountId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    seedSubscription(accountId, SubscriptionPlan.FREE, SubscriptionStatus.ACTIVE);

    mockMvc
        .perform(
            trusted(
                patch("/api/v1/subscriptions/current/plan")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                                        { "plan": "pro" }
                                        """),
                accountId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.plan").value("PRO"))
        .andExpect(jsonPath("$.status").value("ACTIVE"))
        .andExpect(jsonPath("$.entitlements[2]").value("ADVANCED_RANKING"));

    Subscription subscription = subscriptionRepository.findByAccountId(accountId).orElseThrow();
    assertThat(subscription.getPlan()).isEqualTo(SubscriptionPlan.PRO);
    assertThat(outboxEventRepository.findAll())
        .extracting(OutboxEvent::getEventType)
        .containsExactly("subscription.changed.v1");
  }

  @Test
  void cancelCurrentSubscriptionMarksItCancelledAndPublishesOutboxEvent() throws Exception {
    UUID accountId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    seedSubscription(accountId, SubscriptionPlan.TEAM, SubscriptionStatus.ACTIVE);

    mockMvc
        .perform(trusted(post("/api/v1/subscriptions/current/cancel"), accountId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.plan").value("TEAM"))
        .andExpect(jsonPath("$.status").value("CANCELLED"));

    Subscription subscription = subscriptionRepository.findByAccountId(accountId).orElseThrow();
    assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);
    assertThat(outboxEventRepository.findAll())
        .extracting(OutboxEvent::getEventType)
        .containsExactly("subscription.cancelled.v1");
  }

  @Test
  void currentSubscriptionRejectsMissingAccountContext() throws Exception {
    mockMvc
        .perform(get("/api/v1/subscriptions/current").header("X-Arenax-User-Id", UUID.randomUUID()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.message").value("Account context is required"));
  }

  private void seedSubscription(UUID accountId, SubscriptionPlan plan, SubscriptionStatus status) {
    Subscription subscription = new Subscription();
    subscription.setAccountId(accountId);
    subscription.setPlan(plan);
    subscription.setStatus(status);
    subscriptionRepository.save(subscription);
  }

  private MockHttpServletRequestBuilder trusted(
      MockHttpServletRequestBuilder request, UUID accountId) {
    return request
        .header("X-Arenax-User-Id", UUID.fromString("11111111-1111-1111-1111-111111111111"))
        .header("X-Arenax-Account-Id", accountId);
  }
}
