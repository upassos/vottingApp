package com.ubione.voting.domain.service;

import com.ubione.voting.domain.entity.SessionStatus;
import com.ubione.voting.domain.entity.VotingSession;
import com.ubione.voting.domain.exception.SessionAlreadyOpenException;
import com.ubione.voting.infra.messaging.VotingEventPublisher;
import com.ubione.voting.infra.repository.VotingSessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SessionService {

    private final VotingSessionRepository sessionRepository;
    private final AgendaService agendaService;
    private final VotingEventPublisher publisher;
    private final Clock clock;
    private final int defaultDurationSeconds;

    public SessionService(VotingSessionRepository sessionRepository,
                          AgendaService agendaService,
                          VotingEventPublisher publisher,
                          Clock clock,
                          @Value("${voting.session.default-duration-seconds}") int defaultDurationSeconds) {
        this.sessionRepository = sessionRepository;
        this.agendaService = agendaService;
        this.publisher = publisher;
        this.clock = clock;
        this.defaultDurationSeconds = defaultDurationSeconds;
    }

    @Transactional
    public VotingSession openSession(Long agendaId, Integer durationSeconds) {
        agendaService.getOrThrow(agendaId);

        Optional<VotingSession> existing =
                sessionRepository.findFirstByAgendaIdAndStatusOrderByOpenedAtDesc(agendaId, SessionStatus.OPEN);
        if (existing.isPresent()) {
            throw new SessionAlreadyOpenException(agendaId);
        }

        int seconds = (durationSeconds == null) ? defaultDurationSeconds : durationSeconds;
        OffsetDateTime now = OffsetDateTime.now(clock).withOffsetSameInstant(ZoneOffset.UTC);

        VotingSession session = new VotingSession();
        session.setAgenda(agendaService.getOrThrow(agendaId));
        session.setOpenedAt(now);
        session.setClosesAt(now.plusSeconds(seconds));
        session.setStatus(SessionStatus.OPEN);

        VotingSession saved = sessionRepository.save(session);
        publisher.publish("session.opened", Map.of(
                "sessionId", saved.getId(),
                "agendaId", agendaId,
                "closesAt", saved.getClosesAt().toString()
        ));
        return saved;
    }

    public Optional<VotingSession> getCurrentOpenSession(Long agendaId) {
        return sessionRepository.findFirstByAgendaIdAndStatusOrderByOpenedAtDesc(agendaId, SessionStatus.OPEN);
    }

    @Scheduled(fixedDelayString = "${voting.scheduler.close-expired-interval-ms}")
    @Transactional
    public void closeExpiredSessions() {
        OffsetDateTime now = OffsetDateTime.now(clock).withOffsetSameInstant(ZoneOffset.UTC);
        List<VotingSession> expired = sessionRepository.findExpiredOpenSessions(now);
        for (VotingSession s : expired) {
            s.setStatus(SessionStatus.CLOSED);
            sessionRepository.save(s);
            publisher.publish("session.closed", Map.of(
                    "sessionId", s.getId(),
                    "agendaId", s.getAgenda().getId()
            ));
        }
    }
}
