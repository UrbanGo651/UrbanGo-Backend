package com.urbango.ridesservice.dto.external;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

/**
 * Representa la estructura de datos de un vehículo como la devuelve driver-service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDto {
    private UUID id;
    private String licensePlate;
    private String vehicleType; // Mantenido como String (o replicar Enum VehicleType aquí)
    private String model;
    private String color;
    private LocalDate soatExpiryDate;
    private LocalDate technoExpiryDate;
    private boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
