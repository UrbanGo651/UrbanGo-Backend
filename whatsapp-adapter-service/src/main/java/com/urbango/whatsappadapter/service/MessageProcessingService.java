package com.urbango.whatsappadapter.service;

public interface MessageProcessingService {

    /**
     * Procesa un mensaje entrante recibido desde WhatsApp/Twilio.
     * Identifica al remitente, interpreta el mensaje y orquesta las llamadas
     * a los microservicios correspondientes (users, drivers, rides).
     *
     * @param fromNumber El número del remitente (ej: whatsapp:+573001234567).
     * @param toNumber El número de Twilio al que se envió el mensaje.
     * @param messageBody El texto del mensaje enviado por el usuario/conductor.
     */
    void processIncomingMessage(String fromNumber, String toNumber, String messageBody);
}
