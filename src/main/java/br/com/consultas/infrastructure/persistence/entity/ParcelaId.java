package br.com.consultas.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Chave composta da entidade Parcela (numeroOperacao + numeroParcela).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ParcelaId implements Serializable {

    private Long numeroOperacao;
    private Integer numeroParcela;
}
