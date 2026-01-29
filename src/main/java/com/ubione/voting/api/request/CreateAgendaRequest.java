package com.ubione.voting.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "CreateAgendaRequest")
public record CreateAgendaRequest(
        @Schema(example = "New board election", description = "Short title for the agenda")
        @NotBlank
        @Size(max = 120)
        String title,

        @Schema(example = "We will vote for the next board members.", description = "Optional description")
        @Size(max = 1000)
        String description
) {}
