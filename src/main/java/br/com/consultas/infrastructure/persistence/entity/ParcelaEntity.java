package br.com.consultas.infrastructure.persistence.entity;

import br.com.consultas.domain.model.StatusParcela;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Entidade JPA para parcela (tb_parcela). Relação N:1 com tb_operacao.
 */
@Entity
@Table(name = "tb_parcela")
@IdClass(ParcelaId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = { "numeroOperacao", "numeroParcela" })
public class ParcelaEntity {

    @Id
    @Column(name = "numero_operacao", nullable = false)
    private Long numeroOperacao;

    @Id
    @Column(name = "numero_parcela", nullable = false)
    private Integer numeroParcela;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "numero_operacao", insertable = false, updatable = false)
    private OperacaoEntity operacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_parcela", nullable = false)
    private StatusParcela statusParcela;

    @Column(name = "valor_principal_parcela", nullable = false, precision = 19, scale = 4)
    private BigDecimal valorPrincipalParcela;

    @Column(name = "valor_juro_principal_parcela", nullable = false, precision = 19, scale = 4)
    private BigDecimal valorJuroPrincipalParcela;
}
