package com.bk.arenax.ranking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bk.arenax.messaging.EventEnvelope;
import com.bk.arenax.ranking.messaging.MatchCompletedPayload;
import com.bk.arenax.ranking.repository.PlayerRankingRepository;
import com.bk.arenax.ranking.repository.RankingHistoryRepository;
import com.bk.arenax.ranking.service.MatchCompletedHandler;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class MatchCompletedHandlerIntegrationTests {

    @Autowired
    private MatchCompletedHandler matchCompletedHandler;

    @Autowired
    private PlayerRankingRepository playerRankingRepository;

    @Autowired
    private RankingHistoryRepository rankingHistoryRepository;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        rankingHistoryRepository.deleteAll();
        playerRankingRepository.deleteAll();
    }

    @Test
    void handleUpdatesWinnerAndLoserRankingsAndExposesPlayerQuery() throws Exception {
        UUID winnerId = UUID.fromString("48f39a61-330d-4838-9db8-fadb4bf8a4b5");
        UUID loserId = UUID.fromString("77f75f74-44b8-4934-a5f7-e20610f84fea");

        matchCompletedHandler.handle(rankMatchCompletedEvent(winnerId, loserId));

        assertThat(playerRankingRepository.findByUserId(winnerId).orElseThrow().getRating()).isEqualTo(1016);
        assertThat(playerRankingRepository.findByUserId(loserId).orElseThrow().getRating()).isEqualTo(984);
        assertThat(rankingHistoryRepository.findAll()).hasSize(2);

        mockMvc.perform(get("/api/v1/rankings/users/{userId}", winnerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(winnerId.toString()))
                .andExpect(jsonPath("$.rating").value(1016))
                .andExpect(jsonPath("$.wins").value(1))
                .andExpect(jsonPath("$.losses").value(0));
    }

    @Test
    void handleIsIdempotentForDuplicateMatchEvent() {
        UUID winnerId = UUID.fromString("4a9d4c0c-6318-4fe2-b127-d81112610240");
        UUID loserId = UUID.fromString("9d3da3ad-50f6-44a6-87da-91cc5b96161a");
        EventEnvelope<MatchCompletedPayload> event = rankMatchCompletedEvent(winnerId, loserId);

        matchCompletedHandler.handle(event);
        matchCompletedHandler.handle(event);

        assertThat(playerRankingRepository.findByUserId(winnerId).orElseThrow().getRating()).isEqualTo(1016);
        assertThat(playerRankingRepository.findByUserId(loserId).orElseThrow().getRating()).isEqualTo(984);
        assertThat(rankingHistoryRepository.findAll()).hasSize(2);
    }

    private EventEnvelope<MatchCompletedPayload> rankMatchCompletedEvent(UUID winnerId, UUID loserId) {
        return new EventEnvelope<>(
                UUID.fromString("5301fced-c585-4322-8f0e-6876a4324d67"),
                "competition.match-completed.v1",
                1,
                Instant.parse("2026-07-13T10:04:00Z"),
                UUID.fromString("8ea42f83-56ee-4662-a6da-4146f7ea313a"),
                "competition-service",
                new MatchCompletedPayload(
                        UUID.fromString("8ea42f83-56ee-4662-a6da-4146f7ea313a"),
                        "RANK",
                        21,
                        19,
                        List.of(winnerId),
                        List.of(loserId),
                        Instant.parse("2026-07-13T10:04:00Z")));
    }
}
