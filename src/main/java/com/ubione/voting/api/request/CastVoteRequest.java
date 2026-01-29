package com.ubione.voting.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(name = "CastVoteRequest")
public class CastVoteRequest {

    @Schema(example = "12345678909", description = "CPF (11 digits, numbers only)")
    @NotBlank
    @Pattern(regexp = "^\\d{11}$", message = "cpf must be 11 digits")
    private String cpf;

    @Schema(example = "YES", allowableValues = {"YES", "NO"}, description = "Vote choice")
    @NotBlank
    private String choice;

    public String getCpf() { return cpf; }
    public String getChoice() { return choice; }

    public void setCpf(String cpf) { this.cpf = cpf; }
    public void setChoice(String choice) { this.choice = choice; }
}
