package br.com.consultas.infrastructure.web.controller;

import br.com.consultas.application.dto.ParcelaResponse;
import br.com.consultas.application.port.input.ObterOperacaoPort;
import br.com.consultas.application.port.input.ObterParcelasPort;
import br.com.consultas.application.projection.ExpandOptions;
import br.com.consultas.application.projection.SparseFieldSet;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    public OperacaoController(ObterOperacaoPort obterOperacaoPort,
                              ObterParcelasPort obterParcelasPort) {
        this.obterOperacaoPort = obterOperacaoPort;
        this.obterParcelasPort = obterParcelasPort;
    }

    /**
     * Retorna a operação (e opcionalmente parcelas).
     * Sem <code>expand=parcelas</code> retorna só operação; com expand, uma query traz operação + parcelas.
     */
    @GetMapping("/{numeroOperacao}")
    public ResponseEntity<?> obterPorNumero(
            @PathVariable Long numeroOperacao,
            @RequestParam(name = "fields", required = false) String fieldsParam,
            @RequestParam(name = "expand", required = false) String expandParam) {

        SparseFieldSet fields = SparseFieldSet.parse(fieldsParam);
        ExpandOptions expand = ExpandOptions.parse(expandParam);
        return obterOperacaoPort.obterPorNumero(numeroOperacao, fields, expand)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{numeroOperacao}/parcelas")
    public ResponseEntity<List<ParcelaResponse>> obterParcelas(@PathVariable Long numeroOperacao) {
        List<ParcelaResponse> parcelas = obterParcelasPort.obterParcelasPorOperacao(numeroOperacao);
        return ResponseEntity.ok(parcelas);
    }
}
