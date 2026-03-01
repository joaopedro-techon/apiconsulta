package br.com.consultas.infrastructure.web.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Resposta padronizada de erro da API (mensagem amigável ao usuário).
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiError(
        @JsonProperty("timestamp") OffsetDateTime timestamp,
        @JsonProperty("status") int status,
        @JsonProperty("error") String error,
        @JsonProperty("message") String message,
        @JsonProperty("path") String path,
        @JsonProperty("details") List<String> details
) {
    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(OffsetDateTime.now(), status, error, message, path, null);
    }

    public static ApiError of(int status, String error, String message, String path, List<String> details) {
        return new ApiError(OffsetDateTime.now(), status, error, message, path, details);
    }
}
