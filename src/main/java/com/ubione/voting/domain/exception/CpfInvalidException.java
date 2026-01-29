package com.ubione.voting.domain.exception;

public class CpfInvalidException extends BusinessException {
    public CpfInvalidException(String cpf) {
        super("CPF_INVALID", "CPF is invalid: " + cpf);
    }
}
