package com.urbango.ridesservice.dto;

// Asumimos que los Enums de DriverStatus y VehicleType NO están en este servicio.
// Por lo tanto, aquí usaremos Strings, aunque podríamos crear Enums equivalentes
// o depender de un módulo 'common' si quisiéramos compartirlos.
// Por simplicidad, usaremos Strings por ahora.

import com.urbango.ridesservice.enums.RideStatus;
import com.urbango.ridesservice.enums.ServiceType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideDto {

    private UUID id; // ID del viaje
    private UUID userId;
    private UUID assignedDriverId; // Null si no está asignado
    private UUID assignedVehicleId; // Null si no está asignado

    private ServiceType serviceType; // "CAR", "MOTORCYCLE", "DELIVERY"
    private RideStatus rideStatus; // "REQUESTED", "ASSIGNED", "COMPLETED", etc.

    private String originDetails;
    private String destinationDetails;
    private String cancellationReason; // Null si no se canceló

    private Instant createdAt;
    private Instant assignedAt; // Null si no está asignado
    private Instant completedAt; // Null si no está completado
    private Instant cancelledAt; // Null si no está cancelado
    private Instant updatedAt;

    // --- Información Adicional (Podríamos añadirla si es necesaria) ---
    // private String driverName; // Obtenido de driver-service
    // private String driverWhatsapp; // Obtenido de driver-service
    // private String vehicleLicensePlate; // Obtenido de driver-service
}