package com.urbango.notificationservice.dto;

import jakarta.validation.constraints.NotBlank; // Usar NotBlank para Strings requeridos
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder; // Usaremos Builder aquí para flexibilidad
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO para la solicitud de notificación al usuario cuando un viaje es asignado.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // Permite construir el objeto estableciendo los campos que se tengan
public class RideAssignedNotificationRequest {

    @NotNull(message = "El ID de usuario (userId) no puede ser nulo")
    private UUID userId; // Usuario a notificar

    @NotNull(message = "El ID de conductor (driverId) no puede ser nulo")
    private UUID driverId; // Conductor asignado

    @NotNull(message = "El ID de viaje (rideId) no puede ser nulo")
    private UUID rideId;

    // Detalles del conductor/vehículo para el mensaje (enviados por RideService)
    @Size(max = 255)
    private String driverName;

    @Size(max = 25) // Podría validarse con @Pattern si siempre viene en formato E.164
    private String driverWhatsapp; // Número para enlace wa.me

    @Size(max = 100)
    private String vehicleType; // Ej: "MOTORCYCLE"

    @Size(max = 100)
    private String vehicleModel; // Ej: "Honda CB160"

    @Size(max = 50)
    private String vehicleColor; // Ej: "Negro"

    @Size(max = 10)
    private String vehicleLicensePlate; // Ej: "XYZ123"

    // Campo combinado opcional (o construirlo en el servicio de notificación)
    // @Size(max = 255)
    // private String vehicleInfo;
}
