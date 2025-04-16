package com.urbango.notificationservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder; // Usar Builder
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO para la solicitud de notificación al conductor confirmando que obtuvo el viaje.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // Permite construcción flexible
public class RideConfirmedNotificationRequest {

    @NotNull(message = "El ID de conductor (driverId) no puede ser nulo")
    private UUID driverId; // Conductor a notificar

    @NotNull(message = "El ID de usuario (userId) no puede ser nulo")
    private UUID userId; // Usuario asociado al viaje

    @NotNull(message = "El ID de viaje (rideId) no puede ser nulo")
    private UUID rideId;

    // Detalles del usuario/viaje para el mensaje (enviados por RideService)
    @Size(max = 255)
    private String userName;

    @Size(max = 25)
    private String userWhatsapp; // Número para enlace wa.me

    @Size(max = 255)
    private String originDetails;

    // @Size(max = 255)
    // private String destinationDetails; // Podría añadirse si es relevante para el conductor
}
