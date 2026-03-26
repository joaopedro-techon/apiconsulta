package br.com.consultas.infrastructure.http.feign;

import br.com.consultas.application.port.output.ExternalSamplesPort;
import br.com.consultas.infrastructure.http.ExternalServiceException;
import feign.FeignException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ExternalSamplesFeignAdapter implements ExternalSamplesPort {

    private final ExternalSample1FeignClient sample1FeignClient;
    private final ExternalSample2FeignClient sample2FeignClient;

    public ExternalSamplesFeignAdapter(ExternalSample1FeignClient sample1FeignClient,
                                        ExternalSample2FeignClient sample2FeignClient) {
        this.sample1FeignClient = sample1FeignClient;
        this.sample2FeignClient = sample2FeignClient;
    }

    @Override
    @CircuitBreaker(name = "external-http")
    @RateLimiter(name = "external-http")
    @Bulkhead(name = "external-http")
    public Map<String, Object> getSample1(String foo) {
        try {
            return sample1FeignClient.getSample1(foo);
        } catch (FeignException ex) {
            throw new ExternalServiceException(
                    "Erro chamando external-sample-1",
                    ex.status(),
                    ex.contentUTF8()
            );
        }
    }

    @Override
    @CircuitBreaker(name = "external-http")
    @RateLimiter(name = "external-http")
    @Bulkhead(name = "external-http")
    public Map<String, Object> getSample2(String bar) {
        try {
            return sample2FeignClient.getSample2(bar);
        } catch (FeignException ex) {
            throw new ExternalServiceException(
                    "Erro chamando external-sample-2",
                    ex.status(),
                    ex.contentUTF8()
            );
        }
    }
}

