package com.urbango.whatsappadapter.service;

public interface WhatsAppResponseService {

    /**
     * Envía un mensaje de respuesta vía WhatsApp/Twilio.
     * @param to Número E.164 completo del destinatario (ej: +57...).
     * @param body Mensaje a enviar.
     */
    void sendWhatsAppMessage(String to, String body);
}
