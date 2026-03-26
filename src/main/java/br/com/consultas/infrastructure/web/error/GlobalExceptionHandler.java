package br.com.consultas.infrastructure.web.error;

import br.com.consultas.infrastructure.exceptions.InvalidFieldException;
import br.com.consultas.infrastructure.http.ExternalServiceException;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.concurrent.TimeoutException;

/**
 * Normaliza exceções da API em respostas de erro amigáveis (JSON padronizado).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidFieldException.class)
    public ResponseEntity<ApiError> handleInvalidField(InvalidFieldException ex, HttpServletRequest request) {
        ApiError body = ApiError.of(
                HttpStatus.BAD_REQUEST.value(),
                "Parâmetro inválido",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = "Valor inválido para o parâmetro '%s': era esperado um formato válido (ex.: número para numeroOperacao)."
                .formatted(ex.getName());
        ApiError body = ApiError.of(
                HttpStatus.BAD_REQUEST.value(),
                "Requisição inválida",
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Circuit breaker aberto (Resilience4j) - retorna 503 para indicar indisponibilidade temporária.
     */
    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<ApiError> handleCircuitBreakerOpen(CallNotPermittedException ex, HttpServletRequest request) {
        ApiError body = ApiError.of(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Serviço temporariamente indisponível",
                "As consultas ao banco foram temporariamente bloqueadas por excesso de falhas. Tente novamente em instantes.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }

    /**
     * Rate limiter excedido (Resilience4j) - retorna 429 Too Many Requests.
     */
    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ApiError> handleRateLimitExceeded(RequestNotPermitted ex, HttpServletRequest request) {
        ApiError body = ApiError.of(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "Limite de taxa excedido",
                "Muitas requisições no momento. Tente novamente em alguns segundos.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(body);
    }

    /**
     * Bulkhead cheio (Resilience4j) - muitas chamadas concorrentes; retorna 429.
     */
    @ExceptionHandler(BulkheadFullException.class)
    public ResponseEntity<ApiError> handleBulkheadFull(BulkheadFullException ex, HttpServletRequest request) {
        ApiError body = ApiError.of(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "Servidor sobrecarregado",
                "Limite de requisições simultâneas atingido. Tente novamente em instantes.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(body);
    }

    /**
     * Falha ao chamar um serviço externo (HTTP error, corpo inesperado etc.).
     */
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ApiError> handleExternalService(ExternalServiceException ex, HttpServletRequest request) {
        String details = ex.getStatusCode() != null
                ? "status=" + ex.getStatusCode()
                : "erro no request";
        ApiError body = ApiError.of(
                HttpStatus.BAD_GATEWAY.value(),
                "Falha no serviço externo",
                ex.getMessage(),
                request.getRequestURI(),
                java.util.List.of(details)
        );
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
    }

    /**
     * Fail-fast: timeout em chamadas externas (ex.: connect/response timeout).
     */
    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<ApiError> handleTimeout(TimeoutException ex, HttpServletRequest request) {
        ApiError body = ApiError.of(
                HttpStatus.GATEWAY_TIMEOUT.value(),
                "Tempo esgotado",
                "Uma chamada a serviço externo excedeu o tempo limite. Tente novamente mais tarde.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
        ApiError body = ApiError.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro interno",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
