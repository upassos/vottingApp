package com.ubione.voting.infra.http;

import com.ubione.voting.domain.exception.CpfInvalidException;
import com.ubione.voting.domain.exception.CpfUnableToVoteException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class CpfValidationClient {

    private final WebClient webClient;
    private final Duration timeout;

    public CpfValidationClient(WebClient cpfWebClient,
                               @Value("${cpf.validation.timeout-ms}") long timeoutMs) {
        this.webClient = cpfWebClient;
        this.timeout = Duration.ofMillis(timeoutMs);
    }

    public void validateCpfOrThrow(String cpf) {
        CpfStatusResponse response = webClient.get()
                .uri("/users/{cpf}", cpf)
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND,
                        r -> Mono.error(new CpfInvalidException(cpf)))
                .bodyToMono(CpfStatusResponse.class)
                .timeout(timeout)
                .block();

        if (response == null || response.status == null) {
            throw new CpfUnableToVoteException(cpf);
        }
        if (!"ABLE_TO_VOTE".equalsIgnoreCase(response.status)) {
            throw new CpfUnableToVoteException(cpf);
        }
    }

    public static class CpfStatusResponse {
        public String status;
    }
}
