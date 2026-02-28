package br.com.consultas.infrastructure.persistence.adapter;

import br.com.consultas.domain.model.Operacao;
import br.com.consultas.domain.model.Parcela;
import br.com.consultas.domain.port.OperacaoRepositoryPort;
import br.com.consultas.infrastructure.persistence.entity.OperacaoEntity;
import br.com.consultas.infrastructure.persistence.entity.ParcelaEntity;
import br.com.consultas.infrastructure.persistence.repository.OperacaoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Adapter de persistência - implementa o port do domínio usando JPA.
 * Carrega operação com parcelas em uma única consulta.
 */
@Component
public class OperacaoRepositoryAdapter implements OperacaoRepositoryPort {

    private final OperacaoJpaRepository jpaRepository;

    public OperacaoRepositoryAdapter(OperacaoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Operacao> findByNumeroOperacao(Long numeroOperacao) {
        return jpaRepository.findByNumeroOperacaoWithParcelas(numeroOperacao)
                .map(this::toDomain);
    }

    private Operacao toDomain(OperacaoEntity entity) {
        List<Parcela> parcelas = entity.getParcelas().stream()
                .sorted(Comparator.comparing(ParcelaEntity::getNumeroParcela))
                .map(this::toParcelaDomain)
                .toList();
        return new Operacao(
                entity.getNumeroOperacao(),
                entity.getStatus(),
                entity.getDataContratacao(),
                entity.getCodigoMeioCobranca(),
                parcelas
        );
    }

    private Parcela toParcelaDomain(ParcelaEntity entity) {
        return new Parcela(
                entity.getNumeroOperacao(),
                entity.getNumeroParcela(),
                entity.getStatusParcela(),
                entity.getValorPrincipalParcela(),
                entity.getValorJuroPrincipalParcela()
        );
    }
}
