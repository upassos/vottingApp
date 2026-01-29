package com.ubione.voting.domain.exception;

public class VoteAlreadyCastException extends BusinessException {
    public VoteAlreadyCastException(Long agendaId, String cpf) {
        super("VOTE_ALREADY_CAST", "CPF " + cpf + " has already voted on agenda " + agendaId);
    }
}
