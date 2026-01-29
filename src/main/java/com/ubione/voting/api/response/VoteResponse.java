package com.ubione.voting.api.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(name = "VoteResponse")
public class VoteResponse {
    public Long id;
    public Long agendaId;
    public String cpf;
    public String choice;
    public OffsetDateTime createdAt;

    public static VoteResponse of(Long id, Long agendaId, String cpf, String choice, OffsetDateTime createdAt) {
        VoteResponse r = new VoteResponse();
        r.id = id; r.agendaId = agendaId; r.cpf = cpf; r.choice = choice; r.createdAt = createdAt;
        return r;
    }
}
