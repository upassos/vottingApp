package com.ubione.voting.api.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(name = "AgendaResponse")
public class AgendaResponse {
    @Schema(example = "1") public Long id;
    @Schema(example = "New board election") public String title;
    @Schema(example = "We will vote for the next board members.") public String description;
    public OffsetDateTime createdAt;

    public static AgendaResponse of(Long id, String title, String description, OffsetDateTime createdAt) {
        AgendaResponse r = new AgendaResponse();
        r.id = id; r.title = title; r.description = description; r.createdAt = createdAt;
        return r;
    }
}
