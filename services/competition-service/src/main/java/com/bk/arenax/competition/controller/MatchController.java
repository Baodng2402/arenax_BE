package com.bk.arenax.competition.controller;

import jakarta.validation.Valid;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.bk.arenax.competition.dto.request.CompleteMatchRequest;
import com.bk.arenax.competition.dto.request.CreateMatchRequest;
import com.bk.arenax.competition.dto.request.JoinMatchRequest;
import com.bk.arenax.competition.dto.response.MatchResponse;
import com.bk.arenax.competition.service.CompetitionService;

@RestController
@RequestMapping("/api/v1/matches")
public class MatchController {

  private final CompetitionService competitionService;

  public MatchController(CompetitionService competitionService) {
    this.competitionService = competitionService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  MatchResponse create(@Valid @RequestBody CreateMatchRequest request) {
    return competitionService.createMatch(request);
  }

  @PostMapping("/{matchId}/join")
  MatchResponse join(@PathVariable UUID matchId, @Valid @RequestBody JoinMatchRequest request) {
    return competitionService.joinMatch(matchId, request);
  }

  @PostMapping("/{matchId}/complete")
  MatchResponse complete(
      @PathVariable UUID matchId, @Valid @RequestBody CompleteMatchRequest request) {
    return competitionService.completeMatch(matchId, request);
  }
}
