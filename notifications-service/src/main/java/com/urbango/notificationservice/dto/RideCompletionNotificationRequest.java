package com.urbango.notificationservice.dto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

/**
 * DTO para la solicitud de notificación al usuario sobre viaje completado.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideCompletionNotificationRequest {

    @NotNull(message = "El ID de usuario (userId) no puede ser nulo")
    private UUID userId; // Usuario a notificar

    @NotNull(message = "El ID de viaje (rideId) no puede ser nulo")
    private UUID rideId;

    // Podríamos añadir más detalles si fueran necesarios (ej: costo final)
    // private Double finalCost;
}
