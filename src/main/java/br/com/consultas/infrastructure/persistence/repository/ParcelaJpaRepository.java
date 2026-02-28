package br.com.consultas.infrastructure.persistence.repository;

import br.com.consultas.infrastructure.persistence.entity.ParcelaEntity;
import br.com.consultas.infrastructure.persistence.entity.ParcelaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParcelaJpaRepository extends JpaRepository<ParcelaEntity, ParcelaId> {

    List<ParcelaEntity> findByNumeroOperacaoOrderByNumeroParcela(Long numeroOperacao);
}
