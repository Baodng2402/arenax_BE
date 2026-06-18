package com.bk.arenax.domain.match.factory;

import com.bk.arenax.domain.match.Sport;

public interface SportFactory {
  Sport createSport(SportCreationContext sportCreationContext);
}
