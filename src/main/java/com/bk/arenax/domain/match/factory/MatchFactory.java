package com.bk.arenax.domain.match.factory;

import com.bk.arenax.domain.match.Match;
import com.bk.arenax.domain.match.Sport;
import com.bk.arenax.domain.match.strategy.SportMatchStrategy;

public interface MatchFactory {
    Match createMatch(Sport sport, MatchCreationContext matchCreationContext, SportMatchStrategy matchStrategy);
}
