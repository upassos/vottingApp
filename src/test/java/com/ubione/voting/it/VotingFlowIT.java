package com.ubione.voting.it;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.ubione.voting.api.request.CastVoteRequest;
import com.ubione.voting.api.request.CreateAgendaRequest;
import com.ubione.voting.api.request.OpenSessionRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class VotingFlowIT {

    @Container
    static OracleContainer oracle = new OracleContainer("gvenzl/oracle-xe:21-slim")
            .withUsername("voting")
            .withPassword("voting");

    static WireMockServer wireMock;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", oracle::getJdbcUrl);
        r.add("spring.datasource.username", oracle::getUsername);
        r.add("spring.datasource.password", oracle::getPassword);
        r.add("cpf.validation.base-url", () -> "http://localhost:8089");
        r.add("cpf.validation.timeout-ms", () -> "5000");
        r.add("spring.rabbitmq.host", () -> "localhost");
    }

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @BeforeAll
    static void startWireMock() {
        wireMock = new WireMockServer(8089);
        wireMock.start();
        configureFor("localhost", 8089);
    }

    @AfterAll
    static void stopWireMock() {
        if (wireMock != null) wireMock.stop();
    }

    @Test
    void endToEnd_shouldCreateAgenda_openSession_vote_andGetResult() {
        wireMock.stubFor(get(urlPathMatching("/users/\d{11}"))
                .willReturn(okJson("{\"status\":\"ABLE_TO_VOTE\"}")));

        CreateAgendaRequest create = new CreateAgendaRequest();
        create.setTitle("Test Agenda");
        create.setDescription("Integration test");

        ResponseEntity<String> created = rest.postForEntity(url("/api/v1/agendas"), create, String.class);
        Assertions.assertEquals(201, created.getStatusCode().value());
        String location = created.getHeaders().getLocation().toString();
        Long agendaId = Long.valueOf(location.substring(location.lastIndexOf('/') + 1));

        OpenSessionRequest open = new OpenSessionRequest();
        open.setDurationSeconds(60);
        ResponseEntity<String> opened = rest.postForEntity(url("/api/v1/agendas/" + agendaId + "/sessions"), open, String.class);
        Assertions.assertEquals(201, opened.getStatusCode().value());

        CastVoteRequest vote = new CastVoteRequest();
        vote.setCpf("12345678909");
        vote.setChoice("YES");

        ResponseEntity<String> voted = rest.postForEntity(url("/api/v1/agendas/" + agendaId + "/votes"), vote, String.class);
        Assertions.assertEquals(201, voted.getStatusCode().value());

        ResponseEntity<String> result = rest.getForEntity(url("/api/v1/agendas/" + agendaId + "/result"), String.class);
        Assertions.assertEquals(200, result.getStatusCode().value());
        Assertions.assertTrue(result.getBody().contains("\"yes\":1"));
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
