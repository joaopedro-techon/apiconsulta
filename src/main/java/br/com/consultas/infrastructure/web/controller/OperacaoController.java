package br.com.consultas.infrastructure.web.controller;

import br.com.consultas.application.filter.FilterOptions;
import br.com.consultas.application.pagination.KeysetPaginationRequest;
import br.com.consultas.application.port.input.ListarOperacoesPort;
import br.com.consultas.application.port.input.ObterOperacaoPort;
import br.com.consultas.application.port.input.ObterParcelasPort;
import br.com.consultas.infrastructure.projection.ExpandOptions;
import br.com.consultas.infrastructure.projection.SparseFieldSet;
import br.com.consultas.infrastructure.web.controller.response.KeysetPageResponse;
import br.com.consultas.infrastructure.web.controller.response.ParcelaResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

/**
 * Controller REST - projeção e expand no banco (Criteria API).
 * <ul>
 *   <li><code>expand=parcelas</code> — traz parcelas só quando pedido (uma query com LEFT JOIN)</li>
 *   <li><code>fields=operacao(numeroOperacao,status),parcelas(numeroParcela)</code> — formato estruturado</li>
 * </ul>
 */
@RestController
@RequestMapping(path = "/api/operacoes-credito", produces = MediaType.APPLICATION_JSON_VALUE)
public class OperacaoController {

    private final ObterOperacaoPort obterOperacaoPort;
    private final ObterParcelasPort obterParcelasPort;
    private final ListarOperacoesPort listarOperacoesPort;

    public OperacaoController(ObterOperacaoPort obterOperacaoPort,
                              ObterParcelasPort obterParcelasPort,
                              ListarOperacoesPort listarOperacoesPort) {
        this.obterOperacaoPort = obterOperacaoPort;
        this.obterParcelasPort = obterParcelasPort;
        this.listarOperacoesPort = listarOperacoesPort;
    }

    /**
     * Lista operações com paginação por keyset (cursor).
     * REST: <code>GET /api/operacoes-credito?limit=20&cursor=opaqueToken</code>
     * Resposta: <code>data</code> + <code>pagination</code> (nextCursor, hasMore, limit).
     * Cabeçalho <code>Link: rel="next"</code> quando houver próxima página (RFC 5988).
     */
    @GetMapping
    public ResponseEntity<KeysetPageResponse<Map<String, Object>>> listar(
            @RequestParam(name = "limit", required = false) Integer limitParam,
            @RequestParam(name = "cursor", required = false) String cursorParam) {

        KeysetPaginationRequest request = KeysetPaginationRequest.of(cursorParam, limitParam);
        var page = listarOperacoesPort.listar(request);
        KeysetPageResponse<Map<String, Object>> body = KeysetPageResponse.from(page);

        HttpHeaders headers = new HttpHeaders();
        if (page.hasMore() && page.nextCursor() != null) {
            String nextUrl = UriComponentsBuilder.fromPath("/api/operacoes-credito")
                    .queryParam("limit", page.limit())
                    .queryParam("cursor", page.nextCursor())
                    .build()
                    .toUriString();
            headers.add(HttpHeaders.LINK, "<" + nextUrl + ">; rel=\"next\"");
        }

        return ResponseEntity.ok().headers(headers).body(body);
    }

    /**
     * Retorna a operação (e opcionalmente parcelas).
     * <code>expand=parcelas</code> inclui parcelas; <code>filter</code> filtra parcelas (ex.: parcelas.statusParcela:eq:ATIVA).
     */
    @GetMapping("/{numeroOperacao}")
    public ResponseEntity<?> obterPorNumero(
            @PathVariable Long numeroOperacao,
            @RequestParam(name = "fields", required = false) String fieldsParam,
            @RequestParam(name = "expand", required = false) String expandParam,
            @RequestParam(name = "filter", required = false) List<String> filterParams) {

        SparseFieldSet fields = SparseFieldSet.parse(fieldsParam);
        ExpandOptions expand = ExpandOptions.parse(expandParam);
        FilterOptions filters = FilterOptions.parse(filterParams);
        return obterOperacaoPort.obterPorNumero(numeroOperacao, fields, expand, filters)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{numeroOperacao}/parcelas")
    public ResponseEntity<List<ParcelaResponse>> obterParcelas(@PathVariable Long numeroOperacao) {
        List<ParcelaResponse> parcelas = obterParcelasPort.obterParcelasPorOperacao(numeroOperacao);
        return ResponseEntity.ok(parcelas);
    }
}
