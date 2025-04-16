package com.urbango.notificationservice.dto;

import jakarta.validation.constraints.NotEmpty; // Para validar listas
import jakarta.validation.constraints.NotNull;  // Para validar objetos/IDs
import jakarta.validation.constraints.Size;    // Para limitar tamaño de strings
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO para la solicitud de notificación de un nuevo viaje disponible a conductores.
 * Recibido por NotificationController desde otros servicios (ej: RideService).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewRideNotificationRequest {

    @NotNull(message = "El ID del viaje (rideId) no puede ser nulo")
    private UUID rideId;

    @NotEmpty(message = "La lista de IDs de conductor (driverIds) no puede estar vacía")
    private List<@NotNull(message = "El ID del conductor en la lista no puede ser nulo") UUID> driverIds; // Validar que la lista no esté vacía y que los elementos no sean nulos

    // Detalles opcionales para enriquecer el mensaje
    @Size(max = 50, message = "El tipo de servicio no debe exceder 50 caracteres")
    private String serviceType; // Ej: "CAR", "MOTORCYCLE"

    @Size(max = 255, message = "Los detalles de origen no deben exceder 255 caracteres")
    private String originDetails;

    // @Size(max = 255, message = "Los detalles de destino no deben exceder 255 caracteres")
    // private String destinationDetails; // Podríamos añadir destino si el mensaje lo requiere
}
