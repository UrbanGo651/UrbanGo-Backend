package com.urbango.ridesservice.dto.external;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Representa la estructura de datos de un documento de conductor como la devuelve driver-service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverDocumentDto {
    private UUID id;
    private String documentType; // Mantenido como String (o replicar Enum)
    private String verificationStatus; // Mantenido como String (o replicar Enum)
    private String verifierNotes;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private Instant verifiedAt;
    private Instant createdAt;
    private Instant updatedAt;
}
