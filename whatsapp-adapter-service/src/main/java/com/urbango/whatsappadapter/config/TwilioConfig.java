package com.urbango.whatsappadapter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated; // Opcional para validar props

@Configuration
@ConfigurationProperties(prefix = "twilio") // Lee propiedades 'twilio.*' de application.yml
@Data // Lombok getters/setters
// @Validated // Descomenta si añades validaciones a las propiedades (ej @NotBlank)
public class TwilioConfig {
    // @NotBlank // Ejemplo de validación
    private String accountSid;
    // @NotBlank
    private String authToken;
    // @NotBlank
    private String whatsappNumber; // Número DESDE el que se envían las respuestas
}
