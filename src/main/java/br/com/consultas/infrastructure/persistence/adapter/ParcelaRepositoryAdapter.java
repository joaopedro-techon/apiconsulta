package br.com.consultas.infrastructure.persistence.adapter;

import br.com.consultas.application.port.output.ParcelaRepositoryPort;
import br.com.consultas.domain.models.ParcelaDomain;
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
    public List<ParcelaDomain> findByNumeroOperacao(Long numeroOperacao) {
        return jpaRepository.findByNumeroOperacaoOrderByNumeroParcela(numeroOperacao).stream()
                .map(this::toDomain)
                .toList();
    }

    private ParcelaDomain toDomain(ParcelaEntity entity) {
        return new ParcelaDomain(
                entity.getNumeroOperacao(),
                entity.getNumeroParcela(),
                entity.getStatusParcela(),
                entity.getValorPrincipalParcela(),
                entity.getValorJuroPrincipalParcela()
        );
    }
}
