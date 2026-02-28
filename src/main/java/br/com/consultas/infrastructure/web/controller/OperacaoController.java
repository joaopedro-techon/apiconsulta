package br.com.consultas.infrastructure.web.controller;

import br.com.consultas.application.dto.OperacaoCreditoResponse;
import br.com.consultas.application.dto.ParcelaResponse;
import br.com.consultas.application.port.input.ObterOperacaoPort;
import br.com.consultas.application.port.input.ObterParcelasPort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller REST - adapter de entrada HTTP.
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

    @GetMapping("/{numeroOperacao}")
    public ResponseEntity<OperacaoCreditoResponse> obterPorNumero(@PathVariable Long numeroOperacao) {
        return obterOperacaoPort.obterPorNumero(numeroOperacao)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{numeroOperacao}/parcelas")
    public ResponseEntity<List<ParcelaResponse>> obterParcelas(@PathVariable Long numeroOperacao) {
        List<ParcelaResponse> parcelas = obterParcelasPort.obterParcelasPorOperacao(numeroOperacao);
        return ResponseEntity.ok(parcelas);
    }
}
