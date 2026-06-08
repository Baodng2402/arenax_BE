package com.bk.arenax.infrastructure.converter.match;


import com.bk.arenax.domain.account.Account;
import com.bk.arenax.domain.matches.Match;
import com.bk.arenax.domain.matches.MatchSide;
import com.bk.arenax.domain.user.User;
import com.bk.arenax.dto.response.MatchModule.AccountSummaryResponse;
import com.bk.arenax.infrastructure.converter.common.CommonMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CommonMapperConfig.class)
public interface MatchMapper {
    MatchResponse toResponse(Match match);
    MatchResponse.MatchSideResponse toSideResponse(MatchSide side);
    default AccountSummaryResponse toAccountSummary(Account account) {
        if (account == null) {
            return null;
        }
        User owner = account.getOwner();
        return new AccountSummaryResponse(
                account.getId(),
                account.getName(),
                account.getType() == null ? null : account.getType().name(),
                owner == null ? null : new AccountSummaryResponse.UserSummaryResponse(
                        owner.getId(),
                        owner.getDisplayName(),
                        owner.getAvatarUrl()
                )
        );
    }
}