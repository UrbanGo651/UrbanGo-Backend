package com.urbango.ridesservice.entity;

import com.urbango.ridesservice.enums.RideStatus; // Importar Enum
import com.urbango.ridesservice.enums.ServiceType; // Importar Enum
import jakarta.persistence.*; // Importar anotaciones JPA
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rides") // Schema definido en application-dev.yml
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false, nullable = false)
    private UUID id;

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
}