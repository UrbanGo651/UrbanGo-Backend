package com.urbango.whatsappadapter.controller;

import com.urbango.whatsappadapter.service.MessageProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/whatsapp")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final MessageProcessingService messageProcessingService;

    /**
     * Endpoint para recibir webhooks de Twilio (mensajes entrantes de WhatsApp).
     * Espera datos como application/x-www-form-urlencoded.
     */
    @PostMapping(path = "/webhook", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Void> handleIncomingMessage(@RequestParam MultiValueMap<String, String> twilioRequest) {

        String messageSid = twilioRequest.getFirst("MessageSid");
        String from = twilioRequest.getFirst("From"); // Número del remitente (ej: whatsapp:+57...)
        String to = twilioRequest.getFirst("To");     // Número de Twilio (ej: whatsapp:+1...)
        String body = twilioRequest.getFirst("Body"); // Mensaje del usuario

        log.info("Webhook Recibido - SID: {}, From: {}, To: {}, Body: '{}'", messageSid, from, to, body);

        if (from == null || body == null || body.isBlank()) {
            log.warn("Webhook ignorado: Falta 'From' o 'Body' o está vacío.");
            return ResponseEntity.badRequest().build(); // Indicar a Twilio que la solicitud es mala
        }

        try {
            // Delegar procesamiento al servicio. Idealmente asíncrono (@Async)
            // pero por ahora síncrono para simplificar.
            messageProcessingService.processIncomingMessage(from, to, body.trim()); // Enviar body sin espacios extra

            // Responder 204 No Content a Twilio para indicar éxito sin TwiML
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("Error fatal al procesar mensaje entrante SID: {}", messageSid, e);
            // Informar a Twilio de un error interno
            return ResponseEntity.internalServerError().build();
        }
    }
}
