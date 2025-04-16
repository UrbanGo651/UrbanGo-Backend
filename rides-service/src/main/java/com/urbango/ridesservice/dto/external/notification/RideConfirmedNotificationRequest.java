package com.urbango.ridesservice.dto.external.notification;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

/**
 * DTO para solicitar notificación al conductor confirmando que obtuvo el viaje.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideConfirmedNotificationRequest {

    private UUID driverId;
    private UUID userId;
    private UUID rideId;
    // Detalles opcionales del usuario/viaje para mostrar al conductor
    private String userName;
    private String userWhatsapp;
    private String originDetails;
    private String destinationDetails;
}
