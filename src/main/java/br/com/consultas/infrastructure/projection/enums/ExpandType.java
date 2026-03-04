package br.com.consultas.infrastructure.projection.enums;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum ExpandType {

    OPERACAO("operacao", "operacoes"),
    ORIGINACAO("originacao", "originacoes"),
    PARCELA("parcela", "parcelas"),
    AMORTIZACAO("amortizacao", "amortizacoes"),
    RECEBIMENTO("recebimento", "recebimentos"),
    PLANO("plano", "planos"),
    PERFIL_CLIENTE("perfil_cliente", "perfilCliente", "perfil-cliente"),
    SALDO("saldo", "saldos"),
    COMPONENTE("componente", "componentes"),
    MOVIMENTO_FINANCEIRO("movimento_financeiro", "movimentos_financeiro", "movimentoFinanceiro", "movimentosFinanceiro", "movimento-financeiro", "movimentos-financeiro");

    private final Set<String> aliases;

    private static final Set<String> ALL_ALIASES = Arrays.stream(values())
            .flatMap(type -> type.aliases.stream())
            .collect(Collectors.toUnmodifiableSet());

    private static final String ALL_ALIASES_FORMATED = Arrays.stream(values())
            .map(ExpandType::format)
            .collect(Collectors.joining(", "));

    ExpandType(String... aliases) {
        this.aliases = Set.of(aliases);
    }

    public Set<String> aliases() {
        return aliases;
    }

    public static Set<String> allAliases() {
        return ALL_ALIASES;
    }

    public static String allAliasesFormated() {
        return ALL_ALIASES_FORMATED;
    }

    private String format() {
        return String.join("/", aliases);
    }

}
