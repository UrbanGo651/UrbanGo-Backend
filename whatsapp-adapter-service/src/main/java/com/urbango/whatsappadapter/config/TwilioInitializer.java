package com.urbango.whatsappadapter.config;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class TwilioInitializer {

    private final TwilioConfig twilioConfig; // Inyecta la config local

    @PostConstruct
    public void initializeTwilio() {
        String accountSid = twilioConfig.getAccountSid();
        String authToken = twilioConfig.getAuthToken();

        // Misma lógica de validación y log que en notifications-service
        if (accountSid == null || authToken == null || accountSid.startsWith("ACxxx") || authToken.startsWith("your_")) {
            log.warn("******************************************************");
            log.warn("Credenciales de Twilio NO configuradas para whatsapp-adapter-service.");
            log.warn("El adaptador NO podrá enviar respuestas directas por WhatsApp.");
            log.warn("Configura las variables de entorno TWILIO_ACCOUNT_SID y TWILIO_AUTH_TOKEN.");
            log.warn("******************************************************");
        } else {
            try {
                Twilio.init(accountSid, authToken);
                log.info("SDK de Twilio inicializado correctamente para whatsapp-adapter-service.");
            } catch (Exception e) {
                log.error("Error al inicializar el SDK de Twilio en whatsapp-adapter-service: {}", e.getMessage(), e);
            }
        }
    }
}
