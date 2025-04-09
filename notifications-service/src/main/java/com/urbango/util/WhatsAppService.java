package com.urbango.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class WhatsAppService {

    @Value("${whatsapp.apiUrl}")
    private String apiUrl;

    @Value("${whatsapp.authToken}")
    private String authToken;

    @Value("${whatsapp.senderNumber}")
    private String senderNumber;

    private final RestTemplate restTemplate = new RestTemplate();

    public String sendWhatsAppMessage(String recipientNumber, String messageContent) {
        // Configuración de headers y autenticación
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authToken);

        // Configuración del payload de acuerdo con el formato requerido por el proveedor
        Map<String, Object> payload = new HashMap<>();
        payload.put("from", senderNumber);
        payload.put("to", "whatsapp:" + recipientNumber);
        payload.put("type", "text");

        Map<String, String> textContent = new HashMap<>();
        textContent.put("body", messageContent);
        payload.put("text", textContent);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);
            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
                return "Mensaje enviado correctamente a " + recipientNumber;
            } else {
                return "Error al enviar el mensaje. Código de estado: " + response.getStatusCode();
            }
        } catch (Exception e) {
            return "Excepción al enviar el mensaje: " + e.getMessage();
        }
    }
}

