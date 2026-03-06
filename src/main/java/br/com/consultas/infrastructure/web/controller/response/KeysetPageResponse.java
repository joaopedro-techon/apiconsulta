package br.com.consultas.infrastructure.web.controller.response;

import br.com.consultas.application.pagination.KeysetPage;

import java.util.List;

/**
 * Resposta REST para página keyset: <code>data</code> e <code>pagination</code> (RFC-friendly).
 */
public record KeysetPageResponse<T>(
        List<T> data,
        PaginationMeta pagination
) {
    public record PaginationMeta(
            String nextCursor,
            boolean hasMore,
            int limit
    ) {
    }

    public static <T> KeysetPageResponse<T> from(KeysetPage<T> page) {
        PaginationMeta meta = new PaginationMeta(
                page.nextCursor(),
                page.hasMore(),
                page.limit()
        );
        return new KeysetPageResponse<>(page.data(), meta);
    }
}
