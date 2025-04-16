package com.urbango.notificationservice.service;

public interface WhatsAppService {

    /**
     * Envía un mensaje de WhatsApp usando Twilio.
     * @param to Número del destinatario en formato E.164 (ej: whatsapp:+573001234567).
     * @param body Contenido del mensaje.
     * @return true si el mensaje se envió (o se puso en cola) exitosamente a Twilio, false si hubo un error.
     */
    boolean sendMessage(String to, String body);
}
