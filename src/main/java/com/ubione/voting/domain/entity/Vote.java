package com.ubione.voting.domain.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "votes",
       uniqueConstraints = @UniqueConstraint(name = "uk_votes_agenda_cpf", columnNames = {"agenda_id", "cpf"}))
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "agenda_id")
    private Agenda agenda;

    @Column(nullable = false, length = 11)
    private String cpf;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private VoteChoice choice;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public Agenda getAgenda() { return agenda; }
    public String getCpf() { return cpf; }
    public VoteChoice getChoice() { return choice; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public void setAgenda(Agenda agenda) { this.agenda = agenda; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public void setChoice(VoteChoice choice) { this.choice = choice; }
}
