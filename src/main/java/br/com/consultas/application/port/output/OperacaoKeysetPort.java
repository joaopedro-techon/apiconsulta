package br.com.consultas.application.port.output;

import java.util.List;
import java.util.Map;

/**
 * Port de saída para listagem paginada por keyset (cursor) de operações.
 * Ordenação por numeroOperacao ASC; cursor = último numeroOperacao da página anterior.
 */
public interface OperacaoKeysetPort {

    /**
     * Retorna até {@code limit} operações com numeroOperacao &gt; afterNumeroOperacaoExclusive.
     * Se afterNumeroOperacaoExclusive for null, retorna a primeira página.
     *
     * @param afterNumeroOperacaoExclusive chave exclusiva (não incluir na página); null = primeira página
     * @param limit                        tamanho da página (1 a 100)
     * @return dados da página e chave do próximo cursor (null se não houver próxima página)
     */
    KeysetPageResult findPageAfter(Long afterNumeroOperacaoExclusive, int limit);

    /**
     * Resultado interno: lista de operações (mapa de campos) e chave para o próximo cursor.
     */
    record KeysetPageResult(
            List<Map<String, Object>> data,
            Long nextCursorKey
    ) {
    }
}
