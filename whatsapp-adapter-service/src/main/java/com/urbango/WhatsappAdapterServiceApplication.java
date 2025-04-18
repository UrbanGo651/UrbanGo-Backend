package com.urbango;

import com.urbango.whatsappadapter.config.TwilioConfig; // <<< Importar
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties; // <<< Importar
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EnableConfigurationProperties(TwilioConfig.class) // <<< HABILITAR CONFIGPROPERTIES
public class WhatsappAdapterServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(WhatsappAdapterServiceApplication.class, args);
    }
}