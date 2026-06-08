package com.bk.arenax.adapter.service.RankModule;

import com.bk.arenax.adapter.repository.RankModule.MatchRepository;
import com.bk.arenax.domain.matches.Match;
import com.bk.arenax.domain.matches.MatchFormat;
import com.bk.arenax.domain.matches.MatchSide;
import com.bk.arenax.domain.matches.MatchStatus;
import com.bk.arenax.domain.user.User;
import com.bk.arenax.dto.request.MatchModule.CreateMatch;
import com.bk.arenax.dto.response.MatchModule.MatchResponse;
import com.bk.arenax.port.repository.UserRepository;
import com.bk.arenax.port.service.match.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MatchServiceImpl implements MatchService {

    private final UserRepository userRepository;
    private final MatchRepository matchRepository;

    @Override
    public void createMatch(CreateMatch createMatch) {
        if (createMatch.playerIds().size() < 2) {
            throw new RuntimeException("At least 2 players are required to create a match");
        }

        User representPlayer = userRepository.findById(createMatch.playerIds().get(0))
                .orElseThrow(() -> new RuntimeException(
                        "Player with id " + createMatch.playerIds().get(0) + " not found"
                ));

        if (!createMatch.startedAt().isBefore(createMatch.endedAt())) {
            throw new RuntimeException("Started time must be before ended time");
        }

        MatchSide newMatchSide = new MatchSide();
        newMatchSide.setNumberOfPlayers(createMatch.playerIds().size());
        newMatchSide.setRepresentativeUser(representPlayer);



        Match newMatch = new Match();
        newMatch.setMatchFormat(createMatch.matchFormat());
        newMatch.setMatchType(createMatch.matchType());
        newMatch.setMatchStatus(MatchStatus.PENDING);
        newMatch.setSportType(createMatch.sportType());
        newMatch.setStartedAt(createMatch.startedAt());
        newMatch.setEndedAt(createMatch.endedAt());

        if (createMatch.matchFormat() == MatchFormat.CUSTOM) {
            newMatch.setMaxPlayers(createMatch.playerIds().size());
        }

        newMatch.addSide(newMatchSide);

        matchRepository.save(newMatch);
    }

    @Override
    public MatchResponse getMatch(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException(
                        "Match with id " + matchId + " not found"
                ));
        return new MatchResponse(
                match.getId(),
                match.getMatchType(),
                match.getSportType(),
                match.getMatchFormat(),
                match.getMatchResult(),
                match.getMatchStatus(),
                match.getStartedAt(),
                match.getEndedAt(),
                match.getArrivalTime(),
                match.getEstimatedPlayingTime()
        );
    }

    @Override
    public void joinMatch(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException(
                        "Match with id " + matchId + " not found"
                ));

        if (match.getMatchStatus() == MatchStatus.ONGOING) {
            throw new RuntimeException("Match is already ongoing");
        }

        if (match.getMatchStatus() == MatchStatus.FULL) {
            throw new RuntimeException("Match room is already full");
        }

        if (match.getTotalPlayers() >= match.getMatchFormat().getRequiredPlayers()) {
            match.setMatchStatus(MatchStatus.FULL);
        }

        matchRepository.save(match);
    }
}
