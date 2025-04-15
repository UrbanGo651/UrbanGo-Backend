package com.urbango.driverservice.repository;

import com.urbango.driverservice.entity.DriverDocument;
import com.urbango.driverservice.enums.DocumentType;
import com.urbango.driverservice.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DriverDocumentRepository extends JpaRepository<DriverDocument, UUID> {

    // Buscar todos los documentos de un conductor específico
    List<DriverDocument> findByDriverId(UUID driverId);

    // Buscar documentos de un conductor por tipo
    List<DriverDocument> findByDriverIdAndDocumentType(UUID driverId, DocumentType documentType);

    // Buscar documentos de un conductor por estado de verificación
    List<DriverDocument> findByDriverIdAndVerificationStatus(UUID driverId, VerificationStatus verificationStatus);
}
