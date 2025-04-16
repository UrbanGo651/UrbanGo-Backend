package com.urbango;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients // Habilitar escaneo de clientes Feign
public class RidesServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RidesServiceApplication.class, args);
    }
}