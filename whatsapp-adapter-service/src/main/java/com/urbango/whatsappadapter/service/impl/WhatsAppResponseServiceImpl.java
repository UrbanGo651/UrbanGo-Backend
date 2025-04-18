package com.urbango.whatsappadapter.service.impl;

import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.urbango.whatsappadapter.config.TwilioConfig; // Necesitas TwilioConfig aquí
import com.urbango.whatsappadapter.service.WhatsAppResponseService; // Importa tu interfaz
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsAppResponseServiceImpl implements WhatsAppResponseService{

    private final TwilioConfig twilioConfig; // Inyecta la config

    @Override
    public void sendWhatsAppMessage(String to, String body) {
        if (twilioConfig.getAccountSid() == null || twilioConfig.getAuthToken() == null ||
                twilioConfig.getAccountSid().startsWith("ACxxx") || twilioConfig.getAuthToken().startsWith("your_")) {
            log.error("No se puede enviar respuesta. Credenciales de Twilio no configuradas.");
            return; // No intentar enviar
        }
        if (to == null || body == null || body.isBlank()) {
            log.error("Intento de enviar mensaje vacío o a destinatario nulo. To: {}, Body: {}", to, body);
            return;
        }

        try {
            String formattedTo = to.startsWith("whatsapp:") ? to : "whatsapp:" + to;
            String formattedFrom = twilioConfig.getWhatsappNumber(); // Número de Twilio

            PhoneNumber toPhoneNumber = new PhoneNumber(formattedTo);
            PhoneNumber fromPhoneNumber = new PhoneNumber(formattedFrom);

            log.debug("Enviando RESPUESTA WhatsApp desde {} hacia {}: {}", formattedFrom, formattedTo, body);
            Message message = Message.creator(toPhoneNumber, fromPhoneNumber, body).create();
            log.info("Respuesta WhatsApp enviada con SID: {}", message.getSid());

        } catch (ApiException e) {
            log.error("Error API Twilio al enviar RESPUESTA a {}: Código={}, Mensaje={}", to, e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al enviar RESPUESTA WhatsApp a {}: {}", to, e.getMessage(), e);
        }
    }
}
