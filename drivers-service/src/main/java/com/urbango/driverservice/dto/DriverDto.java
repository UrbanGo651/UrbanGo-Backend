package com.urbango.driverservice.dto;

import com.urbango.driverservice.enums.DriverStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverDto {

    private UUID id;
    private String whatsappNumber;
    private String fullName;
    private DriverStatus driverStatus; // Considerar Enum
    // private UUID approvedBy; // Podríamos añadirlo si es necesario exponerlo
    private Instant approvalTimestamp;
    private Instant createdAt;
    private Instant updatedAt;

    // Incluir listas de los DTOs de vehículos y documentos
    private List<VehicleDto> vehicles = new ArrayList<>();
    private List<DriverDocumentDto> documents = new ArrayList<>();
}
