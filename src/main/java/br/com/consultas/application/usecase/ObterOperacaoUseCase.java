package br.com.consultas.application.usecase;

import br.com.consultas.application.filter.FilterOptions;
import br.com.consultas.application.port.input.ObterOperacaoPort;
import br.com.consultas.application.port.output.OperacaoProjectionPort;
import br.com.consultas.application.projection.ExpandOptions;
import br.com.consultas.application.projection.SparseFieldSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Caso de uso: obter operação com projeção, expand e filter no banco (Criteria API).
 */
@Service
public class ObterOperacaoUseCase implements ObterOperacaoPort {

    private final OperacaoProjectionPort projectionPort;

    public ObterOperacaoUseCase(OperacaoProjectionPort projectionPort) {
        this.projectionPort = projectionPort;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<?> obterPorNumero(Long numeroOperacao, SparseFieldSet fields, ExpandOptions expand, FilterOptions filters) {
        return projectionPort.findProjection(numeroOperacao, fields, expand, filters);
    }
}
