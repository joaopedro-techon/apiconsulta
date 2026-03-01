package br.com.consultas.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import java.time.LocalDate;

/**
 * Entidade JPA para amortização (tb_amortizacao). Relação N:1 com tb_operacao.
 */
@Entity
@Table(name = "tb_amortizacao")
@IdClass(AmortizacaoId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = { "numeroOperacao", "numeroParcelaAmortizada", "dataRecebimento" })
public class AmortizacaoEntity {

    @Id
    @Column(name = "numero_operacao", nullable = false)
    private Long numeroOperacao;

    @Id
    @Column(name = "numero_parcela_amortizada", nullable = false)
    private Integer numeroParcelaAmortizada;

    @Id
    @Column(name = "data_recebimento", nullable = false)
    private LocalDate dataRecebimento;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "numero_operacao", insertable = false, updatable = false)
    private OperacaoEntity operacao;

    @Column(name = "valor_principal_amortizado", nullable = false, precision = 19, scale = 4)
    private BigDecimal valorPrincipalAmortizado;

    @Column(name = "valor_juros_principal_amortizado", nullable = false, precision = 19, scale = 4)
    private BigDecimal valorJurosPrincipalAmortizado;

    @Column(name = "indicador_validade_amortizacao", nullable = false)
    private Boolean indicadorValidadeAmortizacao;
}
