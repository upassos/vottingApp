package com.ubione.voting.domain.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "voting_sessions")
public class VotingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "agenda_id")
    private Agenda agenda;

    @Column(name = "opened_at", nullable = false)
    private OffsetDateTime openedAt;

    @Column(name = "closes_at", nullable = false)
    private OffsetDateTime closesAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SessionStatus status;

    public Long getId() { return id; }
    public Agenda getAgenda() { return agenda; }
    public OffsetDateTime getOpenedAt() { return openedAt; }
    public OffsetDateTime getClosesAt() { return closesAt; }
    public SessionStatus getStatus() { return status; }

    public void setAgenda(Agenda agenda) { this.agenda = agenda; }
    public void setOpenedAt(OffsetDateTime openedAt) { this.openedAt = openedAt; }
    public void setClosesAt(OffsetDateTime closesAt) { this.closesAt = closesAt; }
    public void setStatus(SessionStatus status) { this.status = status; }

    public boolean isOpenAt(OffsetDateTime now) {
        return this.status == SessionStatus.OPEN && this.closesAt != null && this.closesAt.isAfter(now);
    }
}

