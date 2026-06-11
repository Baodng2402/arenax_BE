package com.bk.arenax.adapter.rest.RankModule;

import com.bk.arenax.dto.response.ApiResponse;
import com.bk.arenax.dto.response.RankModule.ProfileRankingResponse;
import com.bk.arenax.port.service.ranking.ProfileRankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rank/profile")
@RequiredArgsConstructor

public class ProfileRankingController {
    private final ProfileRankingService profileRankingService;

    @GetMapping
    public ApiResponse<ProfileRankingResponse> getRanking() {
        ProfileRankingResponse response = profileRankingService.getCurentRankingProfile();
        return ApiResponse.of(response);
    }

}
