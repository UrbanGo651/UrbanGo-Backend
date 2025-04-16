package com.urbango.ridesservice.dto.external.notification;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

/**
 * DTO para solicitar notificación al usuario sobre viaje completado.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideCompletionNotificationRequest {
    private UUID userId;
    private UUID rideId;
    // Podríamos añadir detalles del costo, etc., si aplica
}
