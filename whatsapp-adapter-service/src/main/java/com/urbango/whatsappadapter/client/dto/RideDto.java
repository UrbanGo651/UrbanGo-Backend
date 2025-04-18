package com.urbango.whatsappadapter.client.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.UUID;
// No necesitamos Instant aquí si no lo usamos directamente

/**
 * Representa la información mínima esperada de un Ride desde RideServiceClient.
 * Utilizado para deserializar la respuesta del Feign Client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideDto {

    // Necesario para referenciar el viaje
    private UUID id;

    private String shortId;

    // Útil para saber quién lo solicitó
    private UUID userId;

    // Útil para saber quién lo tiene asignado
    private UUID assignedDriverId;

    // Necesitamos el estado para tomar decisiones
    private String rideStatus; // Mantenido como String (o replicar Enum RideStatus)

    // El tipo de servicio puede ser útil
    private String serviceType; // Mantenido como String (o replicar Enum ServiceType)

    // No necesitamos los demás campos (timestamps, detalles de ubicación, etc.)
    // a menos que el adaptador los necesite para alguna lógica específica.
    // private UUID assignedVehicleId;
    // private String originDetails;
    // private String destinationDetails;
    // private String cancellationReason;
    // private java.time.Instant createdAt;
    // ... otros timestamps ...
}
