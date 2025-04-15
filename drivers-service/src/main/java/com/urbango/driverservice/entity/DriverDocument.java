package com.urbango.driverservice.entity;

import com.urbango.driverservice.enums.DocumentType;
import com.urbango.driverservice.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "driver_documents") // Schema definido en application-dev.yml
public class DriverDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false, nullable = false)
    private UUID id;

    // Relación Many-to-One con Conductor
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @Enumerated(EnumType.STRING) // Mapear a VARCHAR
    @Column(name = "document_type", nullable = false, length = 100)
    private DocumentType documentType;

    @Column(name = "file_reference", columnDefinition = "TEXT") // Mapea a TEXT de PostgreSQL
    private String fileReference;

    @Enumerated(EnumType.STRING) // Mapear a VARCHAR
    @Column(name = "verification_status", nullable = false, length = 50)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "verifier_notes", columnDefinition = "TEXT")
    private String verifierNotes;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}