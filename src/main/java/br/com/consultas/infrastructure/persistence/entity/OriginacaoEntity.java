package br.com.consultas.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Entidade JPA para originação (tb_originacao). Relação 1:1 com tb_operacao.
 */
@Entity
@Table(name = "tb_originacao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "numeroOperacao")
public class OriginacaoEntity {

    @Id
    @Column(name = "numero_operacao", nullable = false)
    private Long numeroOperacao;

    @OneToOne
    @MapsId
    @JoinColumn(name = "numero_operacao", nullable = false)
    private OperacaoEntity operacao;

    @Column(name = "taxa_juros", nullable = false, precision = 19, scale = 6)
    private BigDecimal taxaJuros;

    @Column(name = "canal_contratacao", nullable = false, length = 50)
    private String canalContratacao;
}
