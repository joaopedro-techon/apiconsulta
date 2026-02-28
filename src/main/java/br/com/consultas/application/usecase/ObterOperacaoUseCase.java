package br.com.consultas.application.usecase;

import br.com.consultas.application.dto.OperacaoCreditoResponse;
import br.com.consultas.application.dto.OperacaoResponse;
import br.com.consultas.application.dto.ParcelaResponse;
import br.com.consultas.application.port.input.ObterOperacaoPort;
import br.com.consultas.domain.model.Operacao;
import br.com.consultas.domain.model.Parcela;
import br.com.consultas.domain.port.OperacaoRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Caso de uso: obter operação (com parcelas) pelo número.
 */
@Service
public class ObterOperacaoUseCase implements ObterOperacaoPort {

    private final OperacaoRepositoryPort repository;

    public ObterOperacaoUseCase(OperacaoRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OperacaoCreditoResponse> obterPorNumero(Long numeroOperacao) {
        return repository.findByNumeroOperacao(numeroOperacao)
                .map(this::toResponse);
    }

    private OperacaoCreditoResponse toResponse(Operacao operacao) {
        OperacaoResponse operacaoResponse = new OperacaoResponse(
                operacao.numeroOperacao(),
                operacao.status(),
                operacao.dataContratacao(),
                operacao.codigoMeioCobranca()
        );
        List<ParcelaResponse> parcelas = operacao.parcelas().stream()
                .map(this::toParcelaResponse)
                .toList();
        return new OperacaoCreditoResponse(operacaoResponse, parcelas);
    }

    private ParcelaResponse toParcelaResponse(Parcela p) {
        return new ParcelaResponse(
                p.numeroOperacao(),
                p.numeroParcela(),
                p.statusParcela(),
                p.valorPrincipalParcela(),
                p.valorJuroPrincipalParcela()
        );
    }
}
