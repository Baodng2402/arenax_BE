package com.bk.arenax.ranking.controller;

import com.bk.arenax.ranking.dto.response.PlayerRankingResponse;
import com.bk.arenax.ranking.service.RankingQueryService;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rankings")
public class RankingController {

    private final RankingQueryService rankingQueryService;

    public RankingController(RankingQueryService rankingQueryService) {
        this.rankingQueryService = rankingQueryService;
    }

    @GetMapping("/users/{userId}")
    PlayerRankingResponse byUserId(@PathVariable UUID userId) {
        return rankingQueryService.getByUserId(userId);
    }
}
