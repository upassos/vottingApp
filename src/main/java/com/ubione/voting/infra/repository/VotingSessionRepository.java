package com.ubione.voting.infra.repository;

import com.ubione.voting.domain.entity.SessionStatus;
import com.ubione.voting.domain.entity.VotingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface VotingSessionRepository extends JpaRepository<VotingSession, Long> {

    Optional<VotingSession> findFirstByAgendaIdAndStatusOrderByOpenedAtDesc(Long agendaId, SessionStatus status);

    @Query("select s from VotingSession s where s.status = 'OPEN' and s.closesAt <= ?1")
    List<VotingSession> findExpiredOpenSessions(OffsetDateTime now);
}
