package com.bk.arenax.dto.response.RankModule;
public record ProfileRankingResponse(
        String name,
        String rank,
        Integer eloGlobal,
        Integer eloZone,
        Integer winsMatch,
        Integer lossesMatch,
        Integer totalMatch
) {}
