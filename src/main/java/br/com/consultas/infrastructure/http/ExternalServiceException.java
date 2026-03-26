package br.com.consultas.infrastructure.http;

/**
 * Excecao padrao para falhas em chamadas HTTP a servicos externos.
 * Usada no GlobalExceptionHandler para mapear em HTTP 502 (Bad Gateway).
 */
public class ExternalServiceException extends RuntimeException {

    private final Integer statusCode;
    private final String responseBody;

    public ExternalServiceException(String message) {
        super(message);
        this.statusCode = null;
        this.responseBody = null;
    }

    public ExternalServiceException(String message, Integer statusCode, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}

