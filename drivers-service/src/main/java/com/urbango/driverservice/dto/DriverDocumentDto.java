package com.urbango.driverservice.dto;

import com.urbango.driverservice.enums.DocumentType;
import com.urbango.driverservice.enums.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverDocumentDto {

    private UUID id;
    private DocumentType documentType; // Considerar Enum
    private VerificationStatus verificationStatus; // Considerar Enum
    private String verifierNotes;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private Instant verifiedAt;
    private Instant createdAt;
    private Instant updatedAt;
    // No incluimos fileReference (puede ser interno) ni el objeto Driver
}
