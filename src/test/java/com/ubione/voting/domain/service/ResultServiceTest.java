package com.ubione.voting.domain.service;

import com.ubione.voting.domain.entity.SessionStatus;
import com.ubione.voting.infra.repository.VoteRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ResultServiceTest {

    @Test
    void shouldAggregateCountsAndAssumeClosedWhenNoOpenSession() {
        VoteRepository voteRepo = mock(VoteRepository.class);
        SessionService sessionService = mock(SessionService.class);
        AgendaService agendaService = mock(AgendaService.class);

        when(agendaService.getOrThrow(10L)).thenReturn(new com.ubione.voting.domain.entity.Agenda());
        when(voteRepo.countByChoice(10L)).thenReturn(List.of(
                new Object[]{"YES", 7L},
                new Object[]{"NO", 2L}
        ));
        when(sessionService.getCurrentOpenSession(10L)).thenReturn(Optional.empty());

        ResultService service = new ResultService(voteRepo, sessionService, agendaService);
        var res = service.getResult(10L);

        assertEquals(10L, res.agendaId());
        assertEquals(SessionStatus.CLOSED.name(), res.sessionStatus());
        assertEquals(7L, res.yes());
        assertEquals(2L, res.no());
        assertEquals(9L, res.total());
    }
}
