package br.com.consultas.infrastructure.http;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class ApacheHttpClientConfig {

    @Bean(name = "externalApacheHttpClient")
    public CloseableHttpClient externalApacheHttpClient(
            @Value("${external.http.max-connections:200}") int maxConnections,
            @Value("${external.http.connect-timeout-ms:250}") int connectTimeoutMs,
            @Value("${external.http.response-timeout-ms:800}") int responseTimeoutMs) {

        PoolingHttpClientConnectionManagerBuilder cmBuilder = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(maxConnections)
                .setMaxConnPerRoute(maxConnections);

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.of(connectTimeoutMs, TimeUnit.MILLISECONDS))
                // "responseTimeout" protege o tempo total para ler a resposta.
                .setResponseTimeout(Timeout.of(responseTimeoutMs, TimeUnit.MILLISECONDS))
                .build();

        return HttpClients.custom()
                .setConnectionManager(cmBuilder.build())
                .setDefaultRequestConfig(requestConfig)
                .build();
    }
}

