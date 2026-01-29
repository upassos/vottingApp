package com.ubione.voting.domain.exception;

public class CpfUnableToVoteException extends BusinessException {
    public CpfUnableToVoteException(String cpf) {
        super("CPF_UNABLE_TO_VOTE", "CPF is unable to vote: " + cpf);
    }
}
