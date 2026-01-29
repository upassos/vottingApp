package com.ubione.voting.domain.service;

import com.ubione.voting.domain.entity.Agenda;
import com.ubione.voting.domain.entity.SessionStatus;
import com.ubione.voting.domain.entity.VotingSession;
import com.ubione.voting.domain.exception.SessionClosedException;
import com.ubione.voting.domain.exception.VoteAlreadyCastException;
import com.ubione.voting.infra.http.CpfValidationClient;
import com.ubione.voting.infra.messaging.VotingEventPublisher;
import com.ubione.voting.infra.repository.VoteRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VoteServiceTest {

    @Test
    void shouldRejectWhenNoOpenSession() {
        VoteRepository voteRepo = mock(VoteRepository.class);
        AgendaService agendaService = mock(AgendaService.class);
        SessionService sessionService = mock(SessionService.class);
        CpfValidationClient cpfClient = mock(CpfValidationClient.class);
        VotingEventPublisher publisher = mock(VotingEventPublisher.class);

        when(agendaService.getOrThrow(1L)).thenReturn(new Agenda());
        when(sessionService.getCurrentOpenSession(1L)).thenReturn(Optional.empty());

        VoteService service = new VoteService(voteRepo, agendaService, sessionService, cpfClient, publisher, Clock.systemUTC());

        assertThrows(SessionClosedException.class, () -> service.castVote(1L, "12345678909", "YES"));
    }

    @Test
    void shouldRejectDuplicateVoteByUniqueConstraint() {
        VoteRepository voteRepo = mock(VoteRepository.class);
        AgendaService agendaService = mock(AgendaService.class);
        SessionService sessionService = mock(SessionService.class);
        CpfValidationClient cpfClient = mock(CpfValidationClient.class);
        VotingEventPublisher publisher = mock(VotingEventPublisher.class);

        Agenda agenda = new Agenda();
        when(agendaService.getOrThrow(1L)).thenReturn(agenda);

        VotingSession session = new VotingSession();
        session.setStatus(SessionStatus.OPEN);
        session.setClosesAt(OffsetDateTime.now().plusMinutes(5));
        when(sessionService.getCurrentOpenSession(1L)).thenReturn(Optional.of(session));

        doNothing().when(cpfClient).validateCpfOrThrow("12345678909");
        when(voteRepo.save(Mockito.any())).thenThrow(new org.springframework.dao.DataIntegrityViolationException("dup"));

        VoteService service = new VoteService(voteRepo, agendaService, sessionService, cpfClient, publisher, Clock.systemUTC());

        assertThrows(VoteAlreadyCastException.class, () -> service.castVote(1L, "12345678909", "YES"));
    }

    @Test
    void shouldRejectWhenSessionExpired() {
        VoteRepository voteRepo = mock(VoteRepository.class);
        AgendaService agendaService = mock(AgendaService.class);
        SessionService sessionService = mock(SessionService.class);
        CpfValidationClient cpfClient = mock(CpfValidationClient.class);
        VotingEventPublisher publisher = mock(VotingEventPublisher.class);

        Agenda agenda = new Agenda();
        when(agendaService.getOrThrow(1L)).thenReturn(agenda);

        VotingSession session = new VotingSession();
        session.setStatus(SessionStatus.OPEN);
        session.setClosesAt(OffsetDateTime.parse("2024-01-01T00:00:00Z"));

        when(sessionService.getCurrentOpenSession(1L)).thenReturn(Optional.of(session));

        Clock clock = Clock.fixed(Instant.parse("2024-01-02T00:00:00Z"), ZoneOffset.UTC);
        VoteService service = new VoteService(voteRepo, agendaService, sessionService, cpfClient, publisher, clock);

        assertThrows(SessionClosedException.class, () -> service.castVote(1L, "12345678909", "YES"));
    }
}
