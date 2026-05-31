package com.bk.arenax.port.service.ranking;

import com.bk.arenax.dto.response.RankModule.ProfileRankingResponse;

public interface ProfileRankingService {
    ProfileRankingResponse getCurentRankingProfile();
    ProfileRankingResponse getMatch(String findType);
    ProfileRankingResponse getEloZone(Long userId);
    ProfileRankingResponse getEloGlobal(Long userId);
}
