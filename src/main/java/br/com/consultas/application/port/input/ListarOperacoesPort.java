package br.com.consultas.application.port.input;

import br.com.consultas.application.pagination.KeysetPage;
import br.com.consultas.application.pagination.KeysetPaginationRequest;

import java.util.Map;

/**
 * Port de entrada - listagem de operações com paginação por keyset (cursor).
 * REST: GET /api/operacoes-credito?limit=20&cursor=opaqueToken
 */
public interface ListarOperacoesPort {

    /**
     * Lista operações em páginas ordenadas por numeroOperacao.
     *
     * @param request cursor (opaco) e limit
     * @return página de operações (mapas com campos da operação) e metadados de paginação
     */
    KeysetPage<Map<String, Object>> listar(KeysetPaginationRequest request);
}
