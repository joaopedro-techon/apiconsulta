package br.com.consultas.application.usecase;

import br.com.consultas.application.dto.ParcelaResponse;
import br.com.consultas.application.port.input.ObterParcelasPort;
import br.com.consultas.domain.model.Parcela;
import br.com.consultas.domain.port.ParcelaRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Caso de uso: obter parcelas de uma operação pelo número da operação.
 */
@Service
public class ObterParcelasUseCase implements ObterParcelasPort {

    private final ParcelaRepositoryPort parcelaRepository;

    public ObterParcelasUseCase(ParcelaRepositoryPort parcelaRepository) {
        this.parcelaRepository = parcelaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParcelaResponse> obterParcelasPorOperacao(Long numeroOperacao) {
        return parcelaRepository.findByNumeroOperacao(numeroOperacao).stream()
                .map(this::toResponse)
                .toList();
    }

    private ParcelaResponse toResponse(Parcela parcela) {
        return new ParcelaResponse(
                parcela.numeroOperacao(),
                parcela.numeroParcela(),
                parcela.statusParcela(),
                parcela.valorPrincipalParcela(),
                parcela.valorJuroPrincipalParcela()
        );
    }
}
