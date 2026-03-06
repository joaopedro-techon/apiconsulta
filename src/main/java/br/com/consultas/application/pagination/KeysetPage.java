package br.com.consultas.application.pagination;

import java.util.List;

/**
 * Página de resultados com paginação por keyset (cursor).
 * REST: resposta com <code>data</code> e <code>pagination</code> (nextCursor, hasMore, limit).
 */
public record KeysetPage<T>(
        List<T> data,
        String nextCursor,
        boolean hasMore,
        int limit
) {
    public static <T> KeysetPage<T> of(List<T> data, String nextCursor, int limit) {
        boolean hasMore = nextCursor != null && !nextCursor.isBlank();
        return new KeysetPage<>(data, hasMore ? nextCursor : null, hasMore, limit);
    }
}
