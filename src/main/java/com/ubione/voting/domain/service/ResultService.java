package com.ubione.voting.domain.service;

import com.ubione.voting.api.response.ResultResponse;
import com.ubione.voting.domain.entity.SessionStatus;
import com.ubione.voting.infra.repository.VoteRepository;
import org.springframework.stereotype.Service;

@Service
public class ResultService {

    private final VoteRepository voteRepository;
    private final SessionService sessionService;
    private final AgendaService agendaService;

    public ResultService(VoteRepository voteRepository, SessionService sessionService, AgendaService agendaService) {
        this.voteRepository = voteRepository;
        this.sessionService = sessionService;
        this.agendaService = agendaService;
    }

    public ResultResponse getResult(Long agendaId) {
        agendaService.getOrThrow(agendaId);

        long yes = 0, no = 0;
        for (Object[] row : voteRepository.countByChoice(agendaId)) {
            String choice = String.valueOf(row[0]);
            long total = ((Number) row[1]).longValue();
            if ("YES".equalsIgnoreCase(choice)) yes = total;
            if ("NO".equalsIgnoreCase(choice)) no = total;
        }

        String status = sessionService.getCurrentOpenSession(agendaId)
                .map(s -> s.getStatus().name())
                .orElse(SessionStatus.CLOSED.name());

        return ResultResponse.of(agendaId, status, yes, no);
    }
}
