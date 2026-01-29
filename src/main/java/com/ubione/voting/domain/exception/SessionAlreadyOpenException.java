package com.ubione.voting.domain.exception;

public class SessionAlreadyOpenException extends BusinessException {
    public SessionAlreadyOpenException(Long agendaId) {
        super("SESSION_ALREADY_OPEN", "There is already an open session for agenda " + agendaId);
    }
}
