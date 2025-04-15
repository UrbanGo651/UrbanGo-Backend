package com.urbango.driverservice.enums;

public enum VerificationStatus {
    PENDING,    // Pendiente de revisión
    VERIFIED,   // Documento verificado y válido
    REJECTED,   // Documento rechazado
    EXPIRED     // Documento expirado (podría ser un estado derivado)
}
