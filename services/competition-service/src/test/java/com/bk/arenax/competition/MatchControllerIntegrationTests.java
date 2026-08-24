package com.bk.arenax.competition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.bk.arenax.competition.domain.entity.Match;
import com.bk.arenax.competition.domain.entity.OutboxEvent;
import com.bk.arenax.competition.domain.enums.MatchStatus;
import com.bk.arenax.competition.repository.MatchRepository;
import com.bk.arenax.competition.repository.OutboxEventRepository;
import com.bk.arenax.competition.repository.SportRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class MatchControllerIntegrationTests {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private SportRepository sportRepository;

  @Autowired private MatchRepository matchRepository;

  @Autowired private OutboxEventRepository outboxEventRepository;

  @BeforeEach
  void setUp() {
    outboxEventRepository.deleteAll();
    matchRepository.deleteAll();
    sportRepository.deleteAll();
  }

  @Test
  void createJoinAndCompleteRankMatchPublishesMatchCompletedEvent() throws Exception {
    UUID captainId = UUID.fromString("4a9d4c0c-6318-4fe2-b127-d81112610240");
    UUID opponentId = UUID.fromString("9d3da3ad-50f6-44a6-87da-91cc5b96161a");

    UUID sportId = createSport("Football");
    UUID matchId = createMatch(sportId, captainId);

    mockMvc
        .perform(
            post("/api/v1/matches/{matchId}/join", matchId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {
                                  "userId": "%s",
                                  "teamNumber": 2
                                }
                                """
                        .formatted(opponentId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("PENDING"));

    mockMvc
        .perform(
            post("/api/v1/matches/{matchId}/complete", matchId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {
                                  "team1Score": 3,
                                  "team2Score": 1
                                }
                                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("COMPLETED"))
        .andExpect(jsonPath("$.team1Score").value(3))
        .andExpect(jsonPath("$.team2Score").value(1));

    Match match = matchRepository.findById(matchId).orElseThrow();
    List<OutboxEvent> events = outboxEventRepository.findAll();

    assertThat(match.getStatus()).isEqualTo(MatchStatus.COMPLETED);
    assertThat(events).hasSize(1);
    assertThat(events.getFirst().getEventType()).isEqualTo("competition.match-completed.v1");
    assertThat(events.getFirst().getCorrelationId()).isEqualTo(matchId);

    JsonNode payload = objectMapper.readTree(events.getFirst().getPayload());
    assertThat(payload.path("payload").path("matchId").asText()).isEqualTo(matchId.toString());
    assertThat(payload.path("payload").path("winners").get(0).asText())
        .isEqualTo(captainId.toString());
    assertThat(payload.path("payload").path("losers").get(0).asText())
        .isEqualTo(opponentId.toString());
    assertThat(payload.path("payload").path("team1Score").asInt()).isEqualTo(3);
    assertThat(payload.path("payload").path("team2Score").asInt()).isEqualTo(1);
  }

  private UUID createSport(String name) throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/sports")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                                {
                                  "name": "%s"
                                }
                                """
                            .formatted(name)))
            .andExpect(status().isCreated())
            .andReturn();

    return UUID.fromString(
        objectMapper.readTree(result.getResponse().getContentAsString()).path("id").asText());
  }

  private UUID createMatch(UUID sportId, UUID captainId) throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/matches")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                                {
                                  "sportId": "%s",
                                  "matchType": "RANK",
                                  "captainUserId": "%s"
                                }
                                """
                            .formatted(sportId, captainId)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andReturn();

    return UUID.fromString(
        objectMapper.readTree(result.getResponse().getContentAsString()).path("id").asText());
  }
}
