package com.ubione.voting.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

@Schema(name = "OpenSessionRequest")
public record OpenSessionRequest(
        @Schema(example = "120", description = "Session duration in seconds. If omitted, defaults to 60 seconds.")
        @Min(1)
        Integer durationSeconds
) {}
