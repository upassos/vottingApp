package com.ubione.voting.domain.service;

import com.ubione.voting.domain.entity.Agenda;
import com.ubione.voting.domain.exception.NotFoundException;
import com.ubione.voting.infra.repository.AgendaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AgendaService {

    private static final Logger log = LoggerFactory.getLogger(AgendaService.class);

    private final AgendaRepository agendaRepository;

    public AgendaService(AgendaRepository agendaRepository) {
        this.agendaRepository = agendaRepository;
    }

    public Agenda create(String title, String description) {
        Agenda a = new Agenda();
        a.setTitle(title);
        a.setDescription(description);

        Agenda saved = agendaRepository.save(a);
        log.debug("agenda.saved id={}", saved.getId());
        return saved;
    }

    public Agenda getOrThrow(Long id) {
        return agendaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Agenda not found: " + id));
    }

    public Page<Agenda> list(Pageable pageable) {
        return agendaRepository.findAll(pageable);
    }
}
