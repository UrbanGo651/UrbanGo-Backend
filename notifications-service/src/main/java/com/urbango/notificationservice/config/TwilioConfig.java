package com.urbango.notificationservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "twilio") // Lee propiedades bajo el prefijo 'twilio'
@Data // Lombok para getters/setters
public class TwilioConfig {

    private String accountSid;
    private String authToken;
    private String whatsappNumber; // Número desde el que se envían los mensajes
}
