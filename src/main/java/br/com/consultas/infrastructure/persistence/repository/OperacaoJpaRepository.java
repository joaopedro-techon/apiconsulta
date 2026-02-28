package br.com.consultas.infrastructure.persistence.repository;

import br.com.consultas.infrastructure.persistence.entity.OperacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OperacaoJpaRepository extends JpaRepository<OperacaoEntity, Long> {

    @Query("SELECT o FROM OperacaoEntity o LEFT JOIN FETCH o.parcelas WHERE o.numeroOperacao = :numeroOperacao")
    Optional<OperacaoEntity> findByNumeroOperacaoWithParcelas(@Param("numeroOperacao") Long numeroOperacao);
}
