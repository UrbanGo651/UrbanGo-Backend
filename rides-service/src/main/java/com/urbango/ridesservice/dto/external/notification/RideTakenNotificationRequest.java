package com.urbango.ridesservice.dto.external.notification;

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
    private UUID driverId;
    private UUID rideId;
}
