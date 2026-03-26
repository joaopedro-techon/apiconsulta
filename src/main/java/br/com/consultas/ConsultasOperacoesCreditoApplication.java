package br.com.consultas;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableFeignClients
public class ConsultasOperacoesCreditoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsultasOperacoesCreditoApplication.class, args);
    }
}

