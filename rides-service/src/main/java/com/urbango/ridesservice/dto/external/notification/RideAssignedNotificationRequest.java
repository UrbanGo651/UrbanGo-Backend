package com.urbango.ridesservice.dto.external.notification;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

/**
 * DTO para solicitar notificación al usuario sobre viaje asignado.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideAssignedNotificationRequest {

    private UUID userId;
    private UUID driverId;
    private UUID rideId;
    // Detalles opcionales del conductor/vehículo para mostrar al usuario
    private String driverName;
    private String driverWhatsapp;
    private String vehicleInfo; // Ej: "Moto Honda - Placa XYZ123"
    private String vehicleLicensePlate;
    private String vehicleColor;
    private String vehicleModel;
    private String vehicleType;
}
