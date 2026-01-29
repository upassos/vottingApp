package com.ubione.voting.domain.service;

import com.ubione.voting.domain.entity.Vote;
import com.ubione.voting.domain.entity.VoteChoice;
import com.ubione.voting.domain.exception.SessionClosedException;
import com.ubione.voting.domain.exception.VoteAlreadyCastException;
import com.ubione.voting.infra.http.CpfValidationClient;
import com.ubione.voting.infra.messaging.VotingEventPublisher;
import com.ubione.voting.infra.repository.VoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Map;

@Service
public class VoteService {

    private static final Logger log = LoggerFactory.getLogger(VoteService.class);

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
    public Vote castVote(Long agendaId, String cpf, VoteChoice choice) {
        var agenda = agendaService.getOrThrow(agendaId);

        var session = sessionService.getCurrentOpenSession(agendaId)
                .orElseThrow(() -> new SessionClosedException(agendaId));

        var now = OffsetDateTime.now(clock);
        if (!session.isOpenAt(now)) {
            throw new SessionClosedException(agendaId);
        }

        cpfValidationClient.validateCpfOrThrow(cpf);

        var v = new Vote();
        v.setAgenda(agenda);
        v.setCpf(cpf);
        v.setChoice(choice);

        try {
            var saved = voteRepository.save(v);
            publisher.publish("vote.cast", Map.of(
                    "agendaId", agendaId,
                    "cpf", cpf,
                    "choice", choice.name()
            ));
            log.debug("vote.persisted agendaId={} cpf={} voteId={}", agendaId, cpf, saved.getId());
            return saved;
        } catch (DataIntegrityViolationException ex) {
            throw new VoteAlreadyCastException(agendaId, cpf);
        }
    }
}
