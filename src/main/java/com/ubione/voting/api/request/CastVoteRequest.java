package com.ubione.voting.api.request;

import com.ubione.voting.domain.entity.VoteChoice;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(name = "CastVoteRequest")
public record CastVoteRequest(
        @Schema(example = "12345678909", description = "CPF (11 digits, numbers only)")
        @NotBlank
        @Pattern(regexp = "^\\d{11}$", message = "cpf must be 11 digits")
        String cpf,

        @Schema(example = "YES", allowableValues = {"YES", "NO"}, description = "Vote choice")
        VoteChoice choice
) {}
