package com.bk.arenax.competition.controller;

import com.bk.arenax.competition.dto.request.CreateSportRequest;
import com.bk.arenax.competition.dto.response.SportResponse;
import com.bk.arenax.competition.service.CompetitionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sports")
public class SportController {

    private final CompetitionService competitionService;

    public SportController(CompetitionService competitionService) {
        this.competitionService = competitionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    SportResponse create(@Valid @RequestBody CreateSportRequest request) {
        return competitionService.createSport(request);
    }
}
