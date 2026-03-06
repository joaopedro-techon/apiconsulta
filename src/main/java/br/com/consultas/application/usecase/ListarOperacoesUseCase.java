package br.com.consultas.application.usecase;

import br.com.consultas.application.pagination.KeysetCursorCodec;
import br.com.consultas.application.pagination.KeysetPage;
import br.com.consultas.application.pagination.KeysetPaginationRequest;
import br.com.consultas.application.port.input.ListarOperacoesPort;
import br.com.consultas.application.port.output.OperacaoKeysetPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Caso de uso: listar operações com paginação por keyset (cursor).
 * Decodifica o cursor da requisição, chama o port de saída e codifica o próximo cursor na resposta.
 */
@Service
public class ListarOperacoesUseCase implements ListarOperacoesPort {

    private final OperacaoKeysetPort keysetPort;

    public ListarOperacoesUseCase(OperacaoKeysetPort keysetPort) {
        this.keysetPort = keysetPort;
    }

    @Override
    @Transactional(readOnly = true)
    public KeysetPage<Map<String, Object>> listar(KeysetPaginationRequest request) {
        Long afterKey = KeysetCursorCodec.decode(request.cursor());
        OperacaoKeysetPort.KeysetPageResult result = keysetPort.findPageAfter(afterKey, request.limit());
        String nextCursor = KeysetCursorCodec.encode(result.nextCursorKey());
        return KeysetPage.of(result.data(), nextCursor, request.limit());
    }
}
