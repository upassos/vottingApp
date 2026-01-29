package com.ubione.voting.domain.service;

import com.ubione.voting.api.response.ResultResponse;
import com.ubione.voting.domain.entity.SessionStatus;
import com.ubione.voting.domain.entity.VoteChoice;
import com.ubione.voting.infra.repository.VoteRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

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

        Map<VoteChoice, Long> counts = voteRepository.countByChoice(agendaId).stream()
                .collect(Collectors.toMap(
                        row -> VoteChoice.valueOf(String.valueOf(row[0]).toUpperCase()),
                        row -> ((Number) row[1]).longValue()
                ));

        long yes = counts.getOrDefault(VoteChoice.YES, 0L);
        long no = counts.getOrDefault(VoteChoice.NO, 0L);

        String status = sessionService.getCurrentOpenSession(agendaId)
                .map(s -> s.getStatus().name())
                .orElse(SessionStatus.CLOSED.name());

        return ResultResponse.of(agendaId, status, yes, no);
    }
}
