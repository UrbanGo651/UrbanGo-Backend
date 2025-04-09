package com.urbango.service.impl;

import com.urbango.dto.GroupNotificationRequestDto;
import com.urbango.dto.NotificationRequestDto;
import com.urbango.service.NotificationServiceInterface;
import com.urbango.util.WhatsAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationServiceImpl implements NotificationServiceInterface {

    @Autowired
    private WhatsAppService whatsAppService;

    @Override
    public String sendNotification(NotificationRequestDto notificationRequestDto) {
        // En un caso real, podrías tener lógica para verificar el receiverType y seleccionar diferentes canales.
        // En este ejemplo, se asume que el receiverId es el número de teléfono.
        String recipientNumber = notificationRequestDto.getReceiverId();
        return whatsAppService.sendWhatsAppMessage(recipientNumber, notificationRequestDto.getMessage());
    }

    @Override
    public String sendToGroup(GroupNotificationRequestDto request) {
        String botUrl = "http://localhost:3000/send-to-group";

        RestTemplate restTemplate = new RestTemplate();

        // Configurar headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Crear cuerpo de la petición
        Map<String, String> body = new HashMap<>();
        System.out.println("Tipo de grupo : "+request.getGroupType());
        body.put("groupType", request.getGroupType()); // Asegúrate que coincida con el nombre exacto del grupo
        body.put("message", request.getMessage());

        // Crear entidad con cuerpo + headers
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(botUrl, entity, String.class);
            return response.getBody(); // Devuelve respuesta del bot
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Error al enviar mensaje al bot: " + e.getMessage();
        }
    }
}
