package com.urbango.ridesservice.dto.external;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Representa la estructura de datos de un conductor como la devuelve driver-service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverDto {
    private UUID id;
    private String whatsappNumber;
    private String fullName;
    private String driverStatus; // Mantenido como String (o replicar Enum)
    private Instant approvalTimestamp;
    private Instant createdAt;
    private Instant updatedAt;

    // Las listas deben usar los DTOs externos definidos en este mismo paquete
    private List<VehicleDto> vehicles = new ArrayList<>();
    private List<DriverDocumentDto> documents = new ArrayList<>();
}
