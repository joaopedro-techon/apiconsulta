package br.com.consultas.application.projection;

/**
 * Lançada quando os parâmetros <code>fields</code> ou <code>expand</code> são inválidos.
 * O status 400 e o corpo da resposta são definidos pelo {@link br.com.consultas.infrastructure.web.error.GlobalExceptionHandler}.
 */
public class InvalidFieldException extends RuntimeException {

    public InvalidFieldException(String message) {
        super(message);
    }
}
