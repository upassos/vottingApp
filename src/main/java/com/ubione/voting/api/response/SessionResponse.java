package com.ubione.voting.api.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(name = "SessionResponse")
public class SessionResponse {
    public Long id;
    public Long agendaId;
    public String status;
    public OffsetDateTime openedAt;
    public OffsetDateTime closesAt;

    public static SessionResponse of(Long id, Long agendaId, String status, OffsetDateTime openedAt, OffsetDateTime closesAt) {
        SessionResponse r = new SessionResponse();
        r.id = id; r.agendaId = agendaId; r.status = status; r.openedAt = openedAt; r.closesAt = closesAt;
        return r;
    }
}
