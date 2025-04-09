package com.urbango.controller;

import com.urbango.dto.GroupNotificationRequestDto;
import com.urbango.dto.NotificationRequestDto;
import com.urbango.service.NotificationServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationServiceInterface notificationService;

    // 👉 Enviar a un usuario o conductor específico
    @PostMapping("/send-to-user")
    public String sendToUser(@RequestBody NotificationRequestDto request) {
        return notificationService.sendNotification(request);
    }

    // 👉 Enviar a grupo de WhatsApp según tipo
    @PostMapping("/send-to-group")
    public String sendToGroup(@RequestBody GroupNotificationRequestDto request) {
        return notificationService.sendToGroup(request);
    }
}
