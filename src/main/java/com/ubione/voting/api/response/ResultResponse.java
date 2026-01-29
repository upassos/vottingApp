package com.ubione.voting.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ResultResponse")
public record ResultResponse(
        Long agendaId,
        String sessionStatus,
        long yes,
        long no,
        long total
) {
    public static ResultResponse of(Long agendaId, String sessionStatus, long yes, long no) {
        return new ResultResponse(agendaId, sessionStatus, yes, no, yes + no);
    }
}
