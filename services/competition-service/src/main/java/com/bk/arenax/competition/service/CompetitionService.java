package com.bk.arenax.competition.service;

import com.bk.arenax.competition.domain.entity.Match;
import com.bk.arenax.competition.domain.entity.MatchParticipant;
import com.bk.arenax.competition.domain.entity.OutboxEvent;
import com.bk.arenax.competition.domain.entity.Sport;
import com.bk.arenax.competition.domain.enums.MatchStatus;
import com.bk.arenax.competition.domain.enums.MatchType;
import com.bk.arenax.competition.dto.request.CompleteMatchRequest;
import com.bk.arenax.competition.dto.request.CreateMatchRequest;
import com.bk.arenax.competition.dto.request.CreateSportRequest;
import com.bk.arenax.competition.dto.request.JoinMatchRequest;
import com.bk.arenax.competition.dto.response.MatchResponse;
import com.bk.arenax.competition.dto.response.SportResponse;
import com.bk.arenax.competition.messaging.EventEnvelope;
import com.bk.arenax.competition.messaging.MatchCompletedPayload;
import com.bk.arenax.competition.repository.MatchParticipantRepository;
import com.bk.arenax.competition.repository.MatchRepository;
import com.bk.arenax.competition.repository.OutboxEventRepository;
import com.bk.arenax.competition.repository.SportRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompetitionService {

    private final SportRepository sportRepository;
    private final MatchRepository matchRepository;
    private final MatchParticipantRepository matchParticipantRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public CompetitionService(
            SportRepository sportRepository,
            MatchRepository matchRepository,
            MatchParticipantRepository matchParticipantRepository,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper) {
        this.sportRepository = sportRepository;
        this.matchRepository = matchRepository;
        this.matchParticipantRepository = matchParticipantRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public SportResponse createSport(CreateSportRequest request) {
        Sport sport = new Sport();
        sport.setName(request.name().trim());
        sportRepository.save(sport);
        return new SportResponse(sport.getId(), sport.getName());
    }

    @Transactional
    public MatchResponse createMatch(CreateMatchRequest request) {
        sportRepository.findById(request.sportId()).orElseThrow(() -> new EntityNotFoundException("Sport not found"));

        Match match = new Match();
        match.setSportId(request.sportId());
        match.setMatchType(MatchType.valueOf(request.matchType().trim().toUpperCase()));
        match.setStatus(MatchStatus.PENDING);
        matchRepository.save(match);

        MatchParticipant captain = new MatchParticipant();
        captain.setMatchId(match.getId());
        captain.setUserId(request.captainUserId());
        captain.setTeamNumber(1);
        matchParticipantRepository.save(captain);

        return toResponse(match);
    }

    @Transactional
    public MatchResponse joinMatch(UUID matchId, JoinMatchRequest request) {
        Match match = matchRepository.findById(matchId).orElseThrow(() -> new EntityNotFoundException("Match not found"));
        if (match.getStatus() != MatchStatus.PENDING) {
            throw new IllegalStateException("Match is not joinable");
        }
        if (matchParticipantRepository.findByMatchIdAndUserId(matchId, request.userId()).isPresent()) {
            return toResponse(match);
        }

        MatchParticipant participant = new MatchParticipant();
        participant.setMatchId(matchId);
        participant.setUserId(request.userId());
        participant.setTeamNumber(request.teamNumber());
        matchParticipantRepository.save(participant);
        return toResponse(match);
    }

    @Transactional
    public MatchResponse completeMatch(UUID matchId, CompleteMatchRequest request) {
        Match match = matchRepository.findById(matchId).orElseThrow(() -> new EntityNotFoundException("Match not found"));
        match.setTeam1Score(request.team1Score());
        match.setTeam2Score(request.team2Score());
        match.setStatus(MatchStatus.COMPLETED);
        match.setFinishedAt(Instant.now());
        matchRepository.save(match);

        persistMatchCompleted(match);
        return toResponse(match);
    }

    private void persistMatchCompleted(Match match) {
        if (outboxEventRepository.findByEventTypeAndCorrelationId("competition.match-completed.v1", match.getId()).isPresent()) {
            return;
        }

        List<MatchParticipant> participants = matchParticipantRepository.findAllByMatchIdOrderByTeamNumberAscCreatedAtAsc(match.getId());
        List<UUID> winners = participantIdsForTeam(participants, match.getTeam1Score() >= match.getTeam2Score() ? 1 : 2);
        List<UUID> losers = participantIdsForTeam(participants, match.getTeam1Score() >= match.getTeam2Score() ? 2 : 1);

        EventEnvelope<MatchCompletedPayload> envelope = new EventEnvelope<>(
                UUID.randomUUID(),
                "competition.match-completed.v1",
                1,
                match.getFinishedAt(),
                match.getId(),
                "competition-service",
                new MatchCompletedPayload(
                        match.getId(),
                        match.getMatchType().name(),
                        match.getTeam1Score(),
                        match.getTeam2Score(),
                        winners,
                        losers,
                        match.getFinishedAt()));

        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setEventType(envelope.eventType());
        outboxEvent.setEventVersion(envelope.eventVersion());
        outboxEvent.setCorrelationId(envelope.correlationId());
        outboxEvent.setProducer(envelope.producer());
        outboxEvent.setOccurredAt(envelope.occurredAt());
        outboxEvent.setPayload(writePayload(envelope));
        outboxEventRepository.save(outboxEvent);
    }

    private List<UUID> participantIdsForTeam(List<MatchParticipant> participants, int teamNumber) {
        return participants.stream()
                .filter(participant -> participant.getTeamNumber() == teamNumber)
                .sorted(Comparator.comparing(MatchParticipant::getCreatedAt))
                .map(MatchParticipant::getUserId)
                .toList();
    }

    private String writePayload(EventEnvelope<MatchCompletedPayload> envelope) {
        try {
            return objectMapper.writeValueAsString(envelope);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize competition event payload", exception);
        }
    }

    private MatchResponse toResponse(Match match) {
        return new MatchResponse(
                match.getId(),
                match.getStatus().name(),
                match.getMatchType().name(),
                match.getTeam1Score(),
                match.getTeam2Score());
    }
}
