package com.ubione.voting.domain.exception;

public class SessionClosedException extends BusinessException {
    public SessionClosedException(Long agendaId) {
        super("SESSION_CLOSED", "Voting session is closed for agenda " + agendaId);
    }
}
