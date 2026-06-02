package com.bk.arenax.dto.response.MatchModule;
import java.time.Instant;
import java.util.Map;

public record MatchResponse(
        AccountSummaryResponse account1,
        AccountSummaryResponse account2,
        Integer numberPlayerOfAccount1,
        Integer numberPlayerOfAccount2,
        String matchType,
        String sportType,
        String matchesFormat,
        String matchResult,
        String matchStatus,
        AccountSummaryResponse winnerAccount,
                AccountSummaryResponse loserAccount,
        Integer scoreAccount1,
        Integer scoreAccount2,
        Map<String, Object> playersData,
        Instant startedAt,
        Instant endedAt

        ) {}
