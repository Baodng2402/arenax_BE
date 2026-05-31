package com.bk.arenax.adapter.service.RankModule;

import com.bk.arenax.domain.ranking.ProfileRanking;
import com.bk.arenax.dto.response.RankModule.ProfileRankingResponse;
import com.bk.arenax.infrastructure.security.SecurityUtils;
import com.bk.arenax.port.repository.RankModule.ProfileRankingRepository;
import com.bk.arenax.port.repository.UserRepository;
import com.bk.arenax.port.service.ranking.ProfileRankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileRankingServiceImpl implements ProfileRankingService {

    private final ProfileRankingRepository profileRankingRepository;
    private final UserRepository userRepository;

    @Override
    public ProfileRankingResponse getCurentRankingProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (!Boolean.TRUE.equals(userRepository.existsById(userId))) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }

        ProfileRanking pRanking =
                profileRankingRepository
                        .findByUserId(userId)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Profile ranking not found for user id: " + userId));

        return new ProfileRankingResponse(
                pRanking.getUser().getName(),
                pRanking.getRanking(),
                pRanking.getEloGlobal(),
                pRanking.getEloZone(),
                pRanking.getMatches("WINS"),
                pRanking.getMatches("LOSSES"),
                pRanking.getMatches("TOTAL"));
    }

    @Override
    public ProfileRankingResponse getMatch(String findType) {
        return null;
    }

    @Override
    public ProfileRankingResponse getEloZone(Long userId) {
        return null;
    }

    @Override
    public ProfileRankingResponse getEloGlobal(Long userId) {
        return null;
    }
}
