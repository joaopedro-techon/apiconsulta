package br.com.consultas.infrastructure.http;

import br.com.consultas.application.port.output.HttpClientPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Adapter síncrono (bloqueante) com Apache HttpClient 5.
 * Funciona bem com Java 21 Virtual Threads (código simples + I/O bloqueante barato).
 */
@Component
public class ApacheHttpClientAdapter implements HttpClientPort {

    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ApacheHttpClientAdapter(
            CloseableHttpClient externalApacheHttpClient,
            ObjectMapper objectMapper) {
        this.httpClient = externalApacheHttpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    @CircuitBreaker(name = "external-http")
    @RateLimiter(name = "external-http")
    @Bulkhead(name = "external-http")
    public <T> T get(String url, Class<T> responseType) {
        HttpGet request = new HttpGet(url);
        request.addHeader("Accept", "application/json");

        try (var response = httpClient.execute(request, HttpClientContext.create())) {
            int status = response.getCode();
            String body = response.getEntity() != null
                    ? EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8)
                    : null;

            if (status < 200 || status >= 300) {
                throw new ExternalServiceException(
                        "HTTP error calling external service: status=" + status,
                        status,
                        body
                );
            }

            if (responseType == String.class) {
                return responseType.cast(body);
            }

            if (body == null || body.isBlank()) {
                return null;
            }
            return objectMapper.readValue(body, responseType);
        } catch (IOException | ParseException e) {
            throw new ExternalServiceException("I/O error calling external service: " + e.getMessage(), null, null);
        }
    }
}

