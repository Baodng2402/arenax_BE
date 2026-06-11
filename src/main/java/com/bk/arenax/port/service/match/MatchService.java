package com.bk.arenax.port.service.match;

import com.bk.arenax.dto.request.MatchModule.CreateMatch;
import com.bk.arenax.dto.response.MatchModule.MatchResponse;

public interface MatchService {
    public void createMatch(CreateMatch createMatch);
    public MatchResponse getMatch(Long matchId);
    public void joinMatch(Long matchId);
}
