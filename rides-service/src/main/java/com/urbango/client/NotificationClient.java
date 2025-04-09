package com.urbango.client;

import com.urbango.dto.NotificationRequestDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class NotificationClient {

    private final WebClient webClient;

    public NotificationClient(@Value("${notifications.service.url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public void sendNotification(NotificationRequestDto request) {
        webClient.post()
                .uri("/notifications/send-to-group")
                .body(Mono.just(request), NotificationRequestDto.class)
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe(
                        unused -> System.out.println("✅ Notificación enviada exitosamente"),
                        error -> System.err.println("❌ Error al enviar notificación: " + error.getMessage())
                );
    }
}
