package br.com.consultas.infrastructure.persistence.adapter;

import br.com.consultas.domain.model.Parcela;
import br.com.consultas.domain.port.ParcelaRepositoryPort;
import br.com.consultas.infrastructure.persistence.entity.ParcelaEntity;
import br.com.consultas.infrastructure.persistence.repository.ParcelaJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adapter de persistência para parcelas - implementa o port do domínio.
 */
@Component
public class ParcelaRepositoryAdapter implements ParcelaRepositoryPort {

    private final ParcelaJpaRepository jpaRepository;

    public ParcelaRepositoryAdapter(ParcelaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Parcela> findByNumeroOperacao(Long numeroOperacao) {
        return jpaRepository.findByNumeroOperacaoOrderByNumeroParcela(numeroOperacao).stream()
                .map(this::toDomain)
                .toList();
    }

    private Parcela toDomain(ParcelaEntity entity) {
        return new Parcela(
                entity.getNumeroOperacao(),
                entity.getNumeroParcela(),
                entity.getStatusParcela(),
                entity.getValorPrincipalParcela(),
                entity.getValorJuroPrincipalParcela()
        );
    }
}
