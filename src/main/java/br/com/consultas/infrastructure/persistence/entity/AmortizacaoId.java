package br.com.consultas.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Chave composta da entidade Amortizacao (numeroOperacao + numeroParcelaAmortizada + dataRecebimento).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AmortizacaoId implements Serializable {

    private Long numeroOperacao;
    private Integer numeroParcelaAmortizada;
    private LocalDate dataRecebimento;
}
