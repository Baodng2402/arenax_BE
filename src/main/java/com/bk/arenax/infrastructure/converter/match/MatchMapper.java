package com.bk.arenax.infrastructure.converter.match;

import com.bk.arenax.domain.account.Account;
import com.bk.arenax.domain.match.Match;
import com.bk.arenax.domain.user.User;
import com.bk.arenax.dto.response.MatchModule.AccountSummaryResponse;
import com.bk.arenax.dto.response.MatchModule.MatchResponse;
import com.bk.arenax.infrastructure.converter.common.CommonMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CommonMapperConfig.class)
public interface MatchMapper {
  @Mapping(target = "sportId", source = "sport.id")
  @Mapping(target = "sportCode", source = "sport.sportCode")
  @Mapping(target = "sportName", source = "sport.name")
  MatchResponse toResponse(Match match);

  default AccountSummaryResponse toAccountSummary(Account account) {
    if (account == null) {
      return null;
    }
    User owner = account.getOwner();
    return new AccountSummaryResponse(
        account.getId(),
        account.getName(),
        account.getType() == null ? null : account.getType().name(),
        owner == null
            ? null
            : new AccountSummaryResponse.UserSummaryResponse(
                owner.getId(), owner.getDisplayName(), owner.getAvatarUrl()));
  }
}
