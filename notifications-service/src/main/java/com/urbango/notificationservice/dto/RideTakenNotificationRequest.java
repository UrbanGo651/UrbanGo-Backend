package com.urbango.notificationservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

/**
 * DTO para notificar a un conductor que el viaje que intentó aceptar ya fue tomado.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideTakenNotificationRequest {

    @NotNull(message = "El ID de conductor (driverId) no puede ser nulo")
    private UUID driverId; // Conductor a notificar

    @NotNull(message = "El ID de viaje (rideId) no puede ser nulo")
    private UUID rideId; // Viaje que intentó tomar
}
