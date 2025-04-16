package com.urbango;

import com.urbango.notificationservice.config.TwilioConfig; // Importar
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties; // Importar
import org.springframework.cloud.openfeign.EnableFeignClients; // Importar si usas Feign

@SpringBootApplication
@EnableConfigurationProperties(TwilioConfig.class) // Habilitar la clase de config
@EnableFeignClients // Descomentar si este servicio llama a otros con Feign
public class NotificationsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationsServiceApplication.class, args);
    }
}