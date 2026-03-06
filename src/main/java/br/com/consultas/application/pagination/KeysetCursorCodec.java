package br.com.consultas.application.pagination;

import br.com.consultas.infrastructure.exceptions.InvalidFieldException;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Codifica/decodifica cursor opaco para keyset (base64url do Long).
 * O cliente não interpreta o valor; apenas repassa na próxima requisição.
 */
public final class KeysetCursorCodec {

    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private KeysetCursorCodec() {
    }

    /**
     * Codifica a chave (ex.: último numeroOperacao da página) em cursor opaco.
     */
    public static String encode(Long key) {
        if (key == null) {
            return null;
        }
        byte[] bytes = ByteBuffer.allocate(Long.BYTES).putLong(key).array();
        return ENCODER.encodeToString(bytes);
    }

    /**
     * Decodifica o cursor para chave. Cursor inválido ou vazio retorna null (primeira página).
     */
    public static Long decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            byte[] bytes = DECODER.decode(cursor.trim());
            if (bytes.length != Long.BYTES) {
                return null;
            }
            return ByteBuffer.wrap(bytes).getLong();
        } catch (IllegalArgumentException e) {
            throw new InvalidFieldException("Cursor inválido: '" + cursor + "'. Use o valor de 'pagination.nextCursor' da resposta anterior.");
        }
    }
}
