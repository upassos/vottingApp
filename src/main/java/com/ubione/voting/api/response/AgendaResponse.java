package com.ubione.voting.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(name = "AgendaResponse")
public record AgendaResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "New board election") String title,
        @Schema(example = "We will vote for the next board members.") String description,
        OffsetDateTime createdAt
) {}
