package com.bk.arenax.domain.match.factory;

import com.bk.arenax.domain.match.Sport;
import com.bk.arenax.domain.match.strategy.SportMatchStrategy;
import com.bk.arenax.domain.match.strategy.SportStrategy;

public interface SportFactory {
    Sport createSport( SportCreationContext sportCreationContext);
}