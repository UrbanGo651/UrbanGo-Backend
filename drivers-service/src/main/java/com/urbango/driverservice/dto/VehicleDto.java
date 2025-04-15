package com.urbango.driverservice.dto;

import com.urbango.driverservice.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDto {

    private UUID id;
    private String licensePlate;
    private VehicleType vehicleType; // Considerar Enum
    private String model;
    private String color;
    private LocalDate soatExpiryDate;
    private LocalDate technoExpiryDate;
    private boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
    // No incluimos el objeto Driver completo para evitar referencias circulares
}
