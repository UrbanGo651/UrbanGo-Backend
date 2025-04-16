package com.urbango.notificationservice.service.impl;


import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.urbango.notificationservice.config.TwilioConfig;
import com.urbango.notificationservice.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsAppServiceImpl implements WhatsAppService {

    private final TwilioConfig twilioConfig;

    @Override
    public boolean sendMessage(String to, String body) {
        // Validar si las credenciales están configuradas (ya hecho en Initializer, pero doble check)
        if (twilioConfig.getAccountSid() == null || twilioConfig.getAuthToken() == null ||
                twilioConfig.getAccountSid().startsWith("ACxxx") || twilioConfig.getAuthToken().startsWith("your_")) {
            log.error("No se puede enviar mensaje. Credenciales de Twilio no configuradas.");
            return false;
        }

        try {
            // Añadir prefijo 'whatsapp:' si no está presente
            String formattedTo = to.startsWith("whatsapp:") ? to : "whatsapp:" + to;
            String formattedFrom = twilioConfig.getWhatsappNumber(); // Ya debería tener 'whatsapp:'

            PhoneNumber toPhoneNumber = new PhoneNumber(formattedTo);
            PhoneNumber fromPhoneNumber = new PhoneNumber(formattedFrom);

            log.debug("Enviando mensaje de WhatsApp desde {} hacia {}: {}", formattedFrom, formattedTo, body);

            Message message = Message.creator(toPhoneNumber, fromPhoneNumber, body).create();

            log.info("Mensaje de WhatsApp enviado con SID: {}", message.getSid());
            // Podríamos chequear message.getStatus() si quisiéramos ser más estrictos
            return true; // Asumimos éxito si no hay excepción

        } catch (ApiException e) {
            // Manejar errores comunes de Twilio
            log.error("Error de API Twilio al enviar mensaje a {}: Código={}, Mensaje={}", to, e.getCode(), e.getMessage());
            // Podrías querer reintentar para ciertos códigos de error
            return false;
        } catch (Exception e) {
            // Capturar otros posibles errores (ej. número inválido)
            log.error("Error inesperado al enviar mensaje de WhatsApp a {}: {}", to, e.getMessage(), e);
            return false;
        }
    }
}
