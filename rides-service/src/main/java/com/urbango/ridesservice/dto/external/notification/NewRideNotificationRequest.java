package com.urbango.ridesservice.dto.external.notification;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @NotNull
    private UUID rideId;
    @NotEmpty
    private List<@NotNull UUID> driverIds;
    @Size(max = 50) private String serviceType;
    @Size(max = 255) private String originDetails;
    // @Size(max = 255) private String destinationDetails; // <-- ¿Está este campo presente?
}
