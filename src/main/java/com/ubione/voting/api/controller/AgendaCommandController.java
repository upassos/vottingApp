package com.ubione.voting.api.controller;

import com.ubione.voting.api.request.CastVoteRequest;
import com.ubione.voting.api.request.CreateAgendaRequest;
import com.ubione.voting.api.request.OpenSessionRequest;
import com.ubione.voting.api.response.AgendaResponse;
import com.ubione.voting.api.response.SessionResponse;
import com.ubione.voting.api.response.VoteResponse;
import com.ubione.voting.domain.service.AgendaService;
import com.ubione.voting.domain.service.SessionService;
import com.ubione.voting.domain.service.VoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/agendas")
public class AgendaCommandController {

    private static final Logger log = LoggerFactory.getLogger(AgendaCommandController.class);

    private final AgendaService agendaService;
    private final SessionService sessionService;
    private final VoteService voteService;

    public AgendaCommandController(AgendaService agendaService,
                                   SessionService sessionService,
                                   VoteService voteService) {
        this.agendaService = agendaService;
        this.sessionService = sessionService;
        this.voteService = voteService;
    }

    @Operation(summary = "Create an agenda")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping
    public ResponseEntity<AgendaResponse> create(@Valid @RequestBody CreateAgendaRequest req) {
        var a = agendaService.create(req.title(), req.description());
        log.info("agenda.created id={}", a.getId());
        return ResponseEntity.created(URI.create("/api/v1/agendas/" + a.getId()))
                .body(new AgendaResponse(a.getId(), a.getTitle(), a.getDescription(), a.getCreatedAt()));
    }

    @Operation(summary = "Open a voting session for an agenda")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "404", description = "Agenda not found"),
            @ApiResponse(responseCode = "409", description = "Session already open")
    })
    @PostMapping("/{agendaId}/sessions")
    public ResponseEntity<SessionResponse> openSession(@PathVariable Long agendaId,
                                                       @Valid @RequestBody(required = false) OpenSessionRequest req) {
        var s = sessionService.openSession(agendaId, req == null ? null : req.durationSeconds());
        log.info("session.opened agendaId={} sessionId={}", agendaId, s.getId());
        return ResponseEntity.created(URI.create("/api/v1/agendas/" + agendaId + "/sessions/current"))
                .body(new SessionResponse(s.getId(), agendaId, s.getStatus().name(), s.getOpenedAt(), s.getClosesAt()));
    }

    @Operation(summary = "Cast a vote (CPF is the unique voter id)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "404", description = "Agenda not found or CPF invalid"),
            @ApiResponse(responseCode = "409", description = "CPF has already voted"),
            @ApiResponse(responseCode = "422", description = "Session closed or CPF unable to vote")
    })
    @PostMapping("/{agendaId}/votes")
    public ResponseEntity<VoteResponse> vote(@PathVariable Long agendaId, @Valid @RequestBody CastVoteRequest req) {
        var v = voteService.castVote(agendaId, req.cpf(), req.choice());
        log.info("vote.cast agendaId={} voteId={}", agendaId, v.getId());
        return ResponseEntity.created(URI.create("/api/v1/agendas/" + agendaId + "/votes/" + v.getId()))
                .body(new VoteResponse(v.getId(), agendaId, v.getCpf(), v.getChoice().name(), v.getCreatedAt()));
    }
}
