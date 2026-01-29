package com.ubione.voting.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubione.voting.api.request.CreateAgendaRequest;
import com.ubione.voting.domain.entity.Agenda;
import com.ubione.voting.domain.entity.Vote;
import com.ubione.voting.domain.entity.VoteChoice;
import com.ubione.voting.domain.service.AgendaService;
import com.ubione.voting.domain.service.SessionService;
import com.ubione.voting.domain.service.VoteService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AgendaCommandController.class)
class AgendaCommandControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean AgendaService agendaService;
    @MockBean SessionService sessionService;
    @MockBean VoteService voteService;

    @Test
    void shouldCreateAgenda() throws Exception {
        Agenda a = new Agenda();
        a.setTitle("t");
        a.setDescription("d");
        a.setCreatedAt(OffsetDateTime.parse("2024-01-01T00:00:00Z"));
        // emulate id
        java.lang.reflect.Field f = Agenda.class.getDeclaredField("id");
        f.setAccessible(true);
        f.set(a, 1L);

        when(agendaService.create("t","d")).thenReturn(a);

        var body = objectMapper.writeValueAsString(new CreateAgendaRequest("t","d"));

        mvc.perform(post("/api/v1/agendas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/agendas/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("t"));
    }

    @Test
    void shouldCastVote() throws Exception {
        Vote v = new Vote();
        v.setCpf("12345678909");
        v.setChoice(VoteChoice.YES);
        v.setCreatedAt(OffsetDateTime.parse("2024-01-01T00:00:00Z"));
        java.lang.reflect.Field f = Vote.class.getDeclaredField("id");
        f.setAccessible(true);
        f.set(v, 5L);

        when(voteService.castVote(2L, "12345678909", VoteChoice.YES)).thenReturn(v);

        var json = "{\"cpf\":\"12345678909\",\"choice\":\"YES\"}";

        mvc.perform(post("/api/v1/agendas/2/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/agendas/2/votes/5"))
                .andExpect(jsonPath("$.choice").value("YES"))
                .andExpect(jsonPath("$.id").value(5));
    }
}
