package com.urbango.notificationservice.config;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class TwilioInitializer {

    private final TwilioConfig twilioConfig;

    @PostConstruct // Se ejecuta después de que el bean es creado e inicializado
    public void initializeTwilio() {
        String accountSid = twilioConfig.getAccountSid();
        String authToken = twilioConfig.getAuthToken();

        if (accountSid == null || authToken == null || accountSid.startsWith("ACxxx") || authToken.startsWith("your_")) {
            log.warn("******************************************************");
            log.warn("Credenciales de Twilio no configuradas correctamente.");
            log.warn("El servicio de notificaciones NO podrá enviar mensajes.");
            log.warn("Configura las variables de entorno TWILIO_ACCOUNT_SID y TWILIO_AUTH_TOKEN.");
            log.warn("******************************************************");
        } else {
            try {
                Twilio.init(accountSid, authToken);
                log.info("SDK de Twilio inicializado correctamente con Account SID: {}", accountSid.substring(0, 5) + "..."); // No loguear el SID completo
            } catch (Exception e) {
                log.error("Error al inicializar el SDK de Twilio: {}", e.getMessage(), e);
            }
        }
    }
}
