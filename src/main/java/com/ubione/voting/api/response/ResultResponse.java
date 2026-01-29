package com.ubione.voting.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ResultResponse")
public class ResultResponse {
    public Long agendaId;
    public String sessionStatus;
    public long yes;
    public long no;
    public long total;

    public static ResultResponse of(Long agendaId, String sessionStatus, long yes, long no) {
        ResultResponse r = new ResultResponse();
        r.agendaId = agendaId; r.sessionStatus = sessionStatus; r.yes = yes; r.no = no; r.total = yes + no;
        return r;
    }
}
