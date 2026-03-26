package br.com.consultas.infrastructure.http;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class FanoutExecutorConfig {

    @Bean(name = "fanoutExecutor")
    public ExecutorService fanoutExecutor(
            @Value("${fanout.executor.virtual.enabled:true}") boolean enabled) {
        if (!enabled) {
            // Fallback simples: usa executor em plataforma (caso precise desativar virtual threads).
            return Executors.newCachedThreadPool();
        }
        // Virtual threads: cada tarefa bloqueante (HTTP) usa uma thread virtual barata.
        // O limite real de concorrencia fica no Bulkhead/limite de conexoes do HttpClient.
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}

