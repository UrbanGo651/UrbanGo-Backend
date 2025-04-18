package com.urbango.whatsappadapter.client.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.UUID;
// No necesitamos imports de LocalDate/Instant si no los usamos directamente aquí

/**
 * Representa la información mínima esperada de un Vehicle desde DriverServiceClient.
 * Utilizado para deserializar la respuesta del Feign Client (parte de DriverDto).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDto {

    // ID del vehículo, necesario para la lógica de aceptación
    private UUID id;

    // Campo necesario para el filtro en findActiveVehicleId
    private boolean isActive;

    // Otros campos que podrías necesitar si construyes mensajes más detallados
    // private String licensePlate;
    // private String vehicleType;
    // private String model;
    // private String color;
    // private java.time.LocalDate soatExpiryDate;
    // private java.time.LocalDate technoExpiryDate;
    // private java.time.Instant createdAt;
    // private java.time.Instant updatedAt;
}
