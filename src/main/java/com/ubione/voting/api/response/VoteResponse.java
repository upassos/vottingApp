package com.ubione.voting.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(name = "VoteResponse")
public record VoteResponse(
        Long id,
        Long agendaId,
        String cpf,
        String choice,
        OffsetDateTime createdAt
) {}
