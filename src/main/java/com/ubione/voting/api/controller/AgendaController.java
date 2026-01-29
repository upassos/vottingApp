package com.ubione.voting.api.controller;

import com.ubione.voting.api.request.CastVoteRequest;
import com.ubione.voting.api.request.CreateAgendaRequest;
import com.ubione.voting.api.request.OpenSessionRequest;
import com.ubione.voting.api.response.*;
import com.ubione.voting.domain.entity.Agenda;
import com.ubione.voting.domain.entity.VotingSession;
import com.ubione.voting.domain.exception.NotFoundException;
import com.ubione.voting.domain.service.AgendaService;
import com.ubione.voting.domain.service.ResultService;
import com.ubione.voting.domain.service.SessionService;
import com.ubione.voting.domain.service.VoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/agendas")
public class AgendaController {

    private final AgendaService agendaService;
    private final SessionService sessionService;
    private final VoteService voteService;
    private final ResultService resultService;

    public AgendaController(AgendaService agendaService,
                            SessionService sessionService,
                            VoteService voteService,
                            ResultService resultService) {
        this.agendaService = agendaService;
        this.sessionService = sessionService;
        this.voteService = voteService;
        this.resultService = resultService;
    }

    @Operation(summary = "Create an agenda")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping
    public ResponseEntity<AgendaResponse> create(@Valid @RequestBody CreateAgendaRequest req) {
        Agenda a = agendaService.create(req.getTitle(), req.getDescription());
        return ResponseEntity.created(URI.create("/api/v1/agendas/" + a.getId()))
                .body(AgendaResponse.of(a.getId(), a.getTitle(), a.getDescription(), a.getCreatedAt()));
    }

    @Operation(summary = "Get an agenda")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not Found")
    })
    @GetMapping("/{id}")
    public AgendaResponse get(@PathVariable Long id) {
        Agenda a = agendaService.getOrThrow(id);
        return AgendaResponse.of(a.getId(), a.getTitle(), a.getDescription(), a.getCreatedAt());
    }

    @Operation(summary = "List agendas (paged)")
    @GetMapping
    public Page<AgendaResponse> list(Pageable pageable) {
        return agendaService.list(pageable).map(a ->
                AgendaResponse.of(a.getId(), a.getTitle(), a.getDescription(), a.getCreatedAt()));
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
        VotingSession s = sessionService.openSession(agendaId, req == null ? null : req.getDurationSeconds());
        return ResponseEntity.created(URI.create("/api/v1/agendas/" + agendaId + "/sessions/current"))
                .body(SessionResponse.of(s.getId(), agendaId, s.getStatus().name(), s.getOpenedAt(), s.getClosesAt()));
    }

    @Operation(summary = "Get the current open session for an agenda (if any)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "No open session")
    })
    @GetMapping("/{agendaId}/sessions/current")
    public SessionResponse currentSession(@PathVariable Long agendaId) {
        VotingSession s = sessionService.getCurrentOpenSession(agendaId)
                .orElseThrow(() -> new NotFoundException("No open session for agenda " + agendaId));
        return SessionResponse.of(s.getId(), agendaId, s.getStatus().name(), s.getOpenedAt(), s.getClosesAt());
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
        var v = voteService.castVote(agendaId, req.getCpf(), req.getChoice());
        return ResponseEntity.created(URI.create("/api/v1/agendas/" + agendaId + "/votes/" + v.getId()))
                .body(VoteResponse.of(v.getId(), agendaId, v.getCpf(), v.getChoice().name(), v.getCreatedAt()));
    }

    @Operation(summary = "Get voting result (partial if session is still open)")
    @GetMapping("/{agendaId}/result")
    public ResultResponse result(@PathVariable Long agendaId) {
        return resultService.getResult(agendaId);
    }
}
