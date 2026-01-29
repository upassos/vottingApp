package com.ubione.voting.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(name = "SessionResponse")
public record SessionResponse(
        Long id,
        Long agendaId,
        String status,
        OffsetDateTime openedAt,
        OffsetDateTime closesAt
) {}
