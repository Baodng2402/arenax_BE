package com.bk.arenax.domain.match.strategy;

import com.bk.arenax.domain.match.factory.SportCreationContext;

public interface SportStrategy {
  void validateCreate(SportCreationContext context);
}
