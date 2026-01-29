package com.ubione.voting.domain.service;

import com.ubione.voting.domain.entity.Vote;
import com.ubione.voting.domain.entity.VoteChoice;
import com.ubione.voting.domain.entity.VotingSession;
import com.ubione.voting.domain.exception.SessionClosedException;
import com.ubione.voting.domain.exception.VoteAlreadyCastException;
import com.ubione.voting.infra.http.CpfValidationClient;
import com.ubione.voting.infra.messaging.VotingEventPublisher;
import com.ubione.voting.infra.repository.VoteRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Map;

@Service
public class VoteService {

    private final VoteRepository voteRepository;
    private final AgendaService agendaService;
    private final SessionService sessionService;
    private final CpfValidationClient cpfValidationClient;
    private final VotingEventPublisher publisher;
    private final Clock clock;

    public VoteService(VoteRepository voteRepository,
                       AgendaService agendaService,
                       SessionService sessionService,
                       CpfValidationClient cpfValidationClient,
                       VotingEventPublisher publisher,
                       Clock clock) {
        this.voteRepository = voteRepository;
        this.agendaService = agendaService;
        this.sessionService = sessionService;
        this.cpfValidationClient = cpfValidationClient;
        this.publisher = publisher;
        this.clock = clock;
    }

    @Transactional
    public Vote castVote(Long agendaId, String cpf, String choiceRaw) {
        var agenda = agendaService.getOrThrow(agendaId);

        VotingSession session = sessionService.getCurrentOpenSession(agendaId)
                .orElseThrow(() -> new SessionClosedException(agendaId));

        OffsetDateTime now = OffsetDateTime.now(clock);
        if (session.getClosesAt().isBefore(now) || session.getStatus() != com.ubione.voting.domain.entity.SessionStatus.OPEN) {
            throw new SessionClosedException(agendaId);
        }

        cpfValidationClient.validateCpfOrThrow(cpf);

        VoteChoice choice;
        try {
            choice = VoteChoice.valueOf(choiceRaw.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("choice must be YES or NO");
        }

        Vote v = new Vote();
        v.setAgenda(agenda);
        v.setCpf(cpf);
        v.setChoice(choice);

        try {
            Vote saved = voteRepository.save(v);
            publisher.publish("vote.cast", Map.of(
                    "agendaId", agendaId,
                    "cpf", cpf,
                    "choice", choice.name()
            ));
            return saved;
        } catch (DataIntegrityViolationException ex) {
            throw new VoteAlreadyCastException(agendaId, cpf);
        }
    }
}
