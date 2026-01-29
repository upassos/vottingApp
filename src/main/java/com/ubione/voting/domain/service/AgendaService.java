package com.ubione.voting.domain.service;

import com.ubione.voting.domain.entity.Agenda;
import com.ubione.voting.domain.exception.NotFoundException;
import com.ubione.voting.infra.repository.AgendaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AgendaService {

    private final AgendaRepository agendaRepository;

    public AgendaService(AgendaRepository agendaRepository) {
        this.agendaRepository = agendaRepository;
    }

    public Agenda create(String title, String description) {
        Agenda a = new Agenda();
        a.setTitle(title);
        a.setDescription(description);
        return agendaRepository.save(a);
    }

    public Agenda getOrThrow(Long id) {
        return agendaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Agenda not found: " + id));
    }

    public Page<Agenda> list(Pageable pageable) {
        return agendaRepository.findAll(pageable);
    }
}
