package com.ubione.voting.infra.repository;

import com.ubione.voting.domain.entity.Agenda;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgendaRepository extends JpaRepository<Agenda, Long> {
}
