package br.com.consultas.infrastructure.http.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Exemplo de serviço externo 2 (GET).
 * URL base configurável via external.sample2.base-url.
 */
@FeignClient(
        name = "external-sample-2",
        url = "${external.sample2.base-url:https://postman-echo.com}",
        configuration = ExternalFeignOkHttpConfig.class
)
public interface ExternalSample2FeignClient {

    @GetMapping("/get")
    Map<String, Object> getSample2(@RequestParam("bar") String bar);
}

