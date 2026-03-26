package br.com.consultas.infrastructure.http.feign;

import feign.Client;
import feign.okhttp.OkHttpClient;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient.Builder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configura o OkHttp do Feign com pool e timeouts fail-fast.
 */
@Configuration
public class ExternalFeignOkHttpConfig {

    @Bean
    public ConnectionPool connectionPool(
            @Value("${external.http.max-connections:200}") int maxConnections
    ) {
        // Mantém o pool com folga para idle; conexoes ativas ficam limitadas pelo client/pool interno.
        int maxIdleConnections = Math.max(5, maxConnections / 2);
        return new ConnectionPool(maxIdleConnections, 60, TimeUnit.SECONDS);
    }

    @Bean
    public okhttp3.OkHttpClient externalOkHttpClient(
            ConnectionPool connectionPool,
            @Value("${external.http.connect-timeout-ms:250}") long connectTimeoutMs,
            @Value("${external.http.response-timeout-ms:800}") long responseTimeoutMs
    ) {
        long timeoutMs = responseTimeoutMs;
        return new Builder()
                .connectTimeout(connectTimeoutMs, TimeUnit.MILLISECONDS)
                .readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .writeTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .connectionPool(connectionPool)
                .build();
    }

    @Bean
    public Client feignClient(okhttp3.OkHttpClient externalOkHttpClient) {
        return new OkHttpClient(externalOkHttpClient);
    }
}

