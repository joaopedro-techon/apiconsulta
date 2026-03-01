package br.com.consultas.infrastructure.persistence.adapter;

import br.com.consultas.application.filter.FilterOptions;
import br.com.consultas.application.port.output.OperacaoProjectionPort;
import br.com.consultas.application.projection.ExpandOptions;
import br.com.consultas.application.projection.OperacaoProjectionResult;
import br.com.consultas.application.projection.SparseFieldSet;
import br.com.consultas.infrastructure.persistence.projection.OperacaoProjectionQueryBuilder;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Adapter: projeção com Criteria API; uma única query; filtros aplicados no JOIN.
 */
@Component
public class OperacaoProjectionAdapter implements OperacaoProjectionPort {

    private final OperacaoProjectionQueryBuilder queryBuilder;

    public OperacaoProjectionAdapter(EntityManager entityManager) {
        this.queryBuilder = new OperacaoProjectionQueryBuilder(entityManager);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OperacaoProjectionResult> findProjection(Long numeroOperacao, SparseFieldSet fields, ExpandOptions expand, FilterOptions filterOptions) {
        return queryBuilder.execute(numeroOperacao, fields, expand, filterOptions);
    }
}
