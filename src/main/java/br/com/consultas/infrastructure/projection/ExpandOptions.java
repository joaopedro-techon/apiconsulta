package br.com.consultas.infrastructure.projection;

import br.com.consultas.infrastructure.exceptions.InvalidFieldException;
import br.com.consultas.infrastructure.projection.enums.ExpandType;
import lombok.Getter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public final class ExpandOptions {

    private static final boolean EXPAND_OBRIGATORIO = true;

    private final boolean includeOperacao;
    private final boolean includeOriginacao;
    private final boolean includeParcelas;
    private final boolean includeAmortizacoes;
    private final boolean includeRecebimentos;
    private final boolean includePlano;
    private final boolean includePerfilCliente;
    private final boolean includeSaldo;
    private final boolean includeComponentes;
    private final boolean includeMovimentosFinanceiro;

    private ExpandOptions(
            boolean includeParcelas,
            boolean includeAmortizacoes,
            boolean includeRecebimentos,
            boolean includePlano,
            boolean includePerfilCliente,
            boolean includeSaldo,
            boolean includeComponentes,
            boolean includeMovimentosFinanceiro
    ) {
        this.includeOperacao = EXPAND_OBRIGATORIO;
        this.includeOriginacao = EXPAND_OBRIGATORIO;
        this.includeParcelas = includeParcelas;
        this.includeAmortizacoes = includeAmortizacoes;
        this.includeRecebimentos = includeRecebimentos;
        this.includePlano = includePlano;
        this.includePerfilCliente = includePerfilCliente;
        this.includeSaldo = includeSaldo;
        this.includeComponentes = includeComponentes;
        this.includeMovimentosFinanceiro = includeMovimentosFinanceiro;
    }

    private ExpandOptions() {
        this.includeOperacao = EXPAND_OBRIGATORIO;
        this.includeOriginacao = EXPAND_OBRIGATORIO;
        this.includeParcelas = false;
        this.includeAmortizacoes = false;
        this.includeRecebimentos = false;
        this.includePlano = false;
        this.includePerfilCliente = false;
        this.includeSaldo = false;
        this.includeComponentes = false;
        this.includeMovimentosFinanceiro = false;
    }

    public static ExpandOptions parse(String expandParam) {
        if (expandParam == null || expandParam.isBlank()) {
            return new ExpandOptions();
        }

        Set<String> requested = Arrays.stream(expandParam.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        Set<String> valid = ExpandType.allAliases();
        Set<String> invalid = requested.stream()
                .filter(r -> !valid.contains(r.toLowerCase()))
                .collect(Collectors.toSet());

        if (!invalid.isEmpty()) {
            throw new InvalidFieldException(String.format("Expand não permitido: " + String.join(", ", invalid) + ". Use: " + ExpandType.allAliasesFormated()));
        }

        return new ExpandOptions(
                requested.stream().anyMatch(r -> ExpandType.PARCELA.aliases().contains(r.toLowerCase())),
                requested.stream().anyMatch(r -> ExpandType.AMORTIZACAO.aliases().contains(r.toLowerCase())),
                requested.stream().anyMatch(r -> ExpandType.RECEBIMENTO.aliases().contains(r.toLowerCase())),
                requested.stream().anyMatch(r -> ExpandType.PLANO.aliases().contains(r.toLowerCase())),
                requested.stream().anyMatch(r -> ExpandType.PERFIL_CLIENTE.aliases().contains(r.toLowerCase())),
                requested.stream().anyMatch(r -> ExpandType.SALDO.aliases().contains(r.toLowerCase())),
                requested.stream().anyMatch(r -> ExpandType.COMPONENTE.aliases().contains(r.toLowerCase())),
                requested.stream().anyMatch(r -> ExpandType.MOVIMENTO_FINANCEIRO.aliases().contains(r.toLowerCase()))
        );
    }

}
