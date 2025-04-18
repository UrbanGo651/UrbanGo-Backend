package com.urbango.ridesservice.entity;

import com.urbango.ridesservice.enums.RideStatus; // Importar Enum
import com.urbango.ridesservice.enums.ServiceType; // Importar Enum
import jakarta.persistence.*; // Importar anotaciones JPA
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rides", indexes = { // Opcional: definir índice aquí también
        @Index(name = "idx_rides_short_id", columnList = "short_id")
})
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "short_id", length = 8, unique = false, updatable = false) // Longitud 8, no único por ahora, no actualizable
    private String shortId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "assigned_driver_id")
    private UUID assignedDriverId;

    @Column(name = "assigned_vehicle_id")
    private UUID assignedVehicleId;

    @Enumerated(EnumType.STRING) // Mapear a VARCHAR
    @Column(name = "service_type", nullable = false, length = 50)
    private ServiceType serviceType; // <<< Tipo cambiado a Enum

    @Enumerated(EnumType.STRING) // Mapear a VARCHAR
    @Column(name = "ride_status", nullable = false, length = 50)
    private RideStatus rideStatus = RideStatus.REQUESTED; // <<< Tipo cambiado a Enum y valor por defecto

    @Column(name = "origin_details", columnDefinition = "TEXT")
    private String originDetails;

    @Column(name = "destination_details", columnDefinition = "TEXT")
    private String destinationDetails;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "assigned_at")
    private Instant assignedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // --- Lógica para generar Short ID ANTES de persistir ---
    @PrePersist // Hook de JPA que se ejecuta antes de guardar una entidad NUEVA
    protected void onCreate() {
        if (this.shortId == null) { // Solo genera si no tiene uno ya
            this.shortId = generateShortId();
        }
        // Asegurarse que el UUID se genera si no lo hace la BD (depende de GenerationType)
        // if (this.id == null) {
        //     this.id = UUID.randomUUID();
        // }
    }

    // Metodo para generar un ID corto aleatorio (ej. 8 caracteres hexadecimales)
    private String generateShortId() {
        // Genera 4 bytes aleatorios seguros
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[4];
        random.nextBytes(bytes);
        // Convierte los bytes a una cadena hexadecimal de 8 caracteres
        return HexFormat.of().formatHex(bytes); // Requiere Java 17+
        // Alternativa para Java < 17:
        // StringBuilder sb = new StringBuilder(8);
        // for (byte b : bytes) {
        //     sb.append(String.format("%02x", b));
        // }
        // return sb.toString();
    }
}