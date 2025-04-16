package com.urbango.ridesservice.dto.external.notification;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO para solicitar el envío de notificación de nuevo viaje a conductores.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewRideNotificationRequest {
    private UUID rideId;
    private List<UUID> driverIds;
    // Podríamos añadir aquí detalles del viaje si son necesarios para la notificación
    // private String originDetails;
    // private String destinationDetails;
    // private String serviceType;
}
