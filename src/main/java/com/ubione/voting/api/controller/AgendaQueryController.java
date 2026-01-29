package com.ubione.voting.api.controller;

import com.ubione.voting.api.response.AgendaResponse;
import com.ubione.voting.api.response.ResultResponse;
import com.ubione.voting.api.response.SessionResponse;
import com.ubione.voting.domain.exception.NotFoundException;
import com.ubione.voting.domain.service.AgendaService;
import com.ubione.voting.domain.service.ResultService;
import com.ubione.voting.domain.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/agendas")
public class AgendaQueryController {

    private final AgendaService agendaService;
    private final SessionService sessionService;
    private final ResultService resultService;

    public AgendaQueryController(AgendaService agendaService, SessionService sessionService, ResultService resultService) {
        this.agendaService = agendaService;
        this.sessionService = sessionService;
        this.resultService = resultService;
    }

    @Operation(summary = "Get an agenda")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not Found")
    })
    @GetMapping("/{id}")
    public AgendaResponse get(@PathVariable Long id) {
        var a = agendaService.getOrThrow(id);
        return new AgendaResponse(a.getId(), a.getTitle(), a.getDescription(), a.getCreatedAt());
    }

    @Operation(summary = "List agendas (paged)")
    @GetMapping
    public Page<AgendaResponse> list(Pageable pageable) {
        return agendaService.list(pageable)
                .map(a -> new AgendaResponse(a.getId(), a.getTitle(), a.getDescription(), a.getCreatedAt()));
    }

    @Operation(summary = "Get the current open session for an agenda (if any)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "No open session")
    })
    @GetMapping("/{agendaId}/sessions/current")
    public SessionResponse currentSession(@PathVariable Long agendaId) {
        var s = sessionService.getCurrentOpenSession(agendaId)
                .orElseThrow(() -> new NotFoundException("No open session for agenda " + agendaId));
        return new SessionResponse(s.getId(), agendaId, s.getStatus().name(), s.getOpenedAt(), s.getClosesAt());
    }

    @Operation(summary = "Get voting result (partial if session is still open)")
    @GetMapping("/{agendaId}/result")
    public ResultResponse result(@PathVariable Long agendaId) {
        return resultService.getResult(agendaId);
    }
}
