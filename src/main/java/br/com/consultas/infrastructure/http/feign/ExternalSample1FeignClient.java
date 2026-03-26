package br.com.consultas.infrastructure.http.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Exemplo de serviço externo 1 (GET).
 * URL base configurável via external.sample1.base-url.
 */
@FeignClient(
        name = "external-sample-1",
        url = "${external.sample1.base-url:https://postman-echo.com}",
        configuration = ExternalFeignOkHttpConfig.class
)
public interface ExternalSample1FeignClient {

    @GetMapping("/get")
    Map<String, Object> getSample1(@RequestParam("foo") String foo);
}

