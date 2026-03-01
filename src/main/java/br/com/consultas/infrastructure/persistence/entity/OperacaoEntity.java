package br.com.consultas.infrastructure.persistence.entity;

import br.com.consultas.domain.model.StatusOperacao;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade JPA para operação (tb_operacao). Relação 1:N com tb_parcela, 1:1 com tb_originacao, 1:N com tb_amortizacao.
 */
@Entity
@Table(name = "tb_operacao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "numeroOperacao")
public class OperacaoEntity {

    @Id
    @Column(name = "numero_operacao", nullable = false)
    private Long numeroOperacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusOperacao status;

    @Column(name = "data_contratacao", nullable = false)
    private LocalDate dataContratacao;

    @Column(name = "codigo_meio_cobranca", nullable = false)
    private int codigoMeioCobranca;

    @OneToMany(mappedBy = "operacao", fetch = FetchType.LAZY)
    private List<ParcelaEntity> parcelas = new ArrayList<>();

    @OneToOne(mappedBy = "operacao", fetch = FetchType.LAZY)
    private OriginacaoEntity originacao;

    @OneToMany(mappedBy = "operacao", fetch = FetchType.LAZY)
    private List<AmortizacaoEntity> amortizacoes = new ArrayList<>();
}
