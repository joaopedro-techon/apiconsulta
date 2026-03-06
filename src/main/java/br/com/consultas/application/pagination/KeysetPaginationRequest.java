package br.com.consultas.application.pagination;

import br.com.consultas.infrastructure.exceptions.InvalidFieldException;

/**
 * Requisição de paginação por keyset (cursor).
 * REST: <code>GET /api/operacoes-credito?limit=20&cursor=opaqueToken</code>
 */
public record KeysetPaginationRequest(
        String cursor,
        int limit
) {
    public static final int DEFAULT_LIMIT = 20;
    public static final int MAX_LIMIT = 100;

    public KeysetPaginationRequest {
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new InvalidFieldException(
                    "Parâmetro 'limit' deve estar entre 1 e " + MAX_LIMIT + ". Recebido: " + limit);
        }
    }

    /**
     * Cria a partir dos parâmetros da requisição (cursor pode ser null/blank).
     */
    public static KeysetPaginationRequest of(String cursorParam, Integer limitParam) {
        String cursor = (cursorParam != null && !cursorParam.isBlank()) ? cursorParam.trim() : null;
        int limit = limitParam != null ? limitParam : DEFAULT_LIMIT;
        return new KeysetPaginationRequest(cursor, limit);
    }
}
