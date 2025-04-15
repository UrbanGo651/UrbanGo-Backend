package com.urbango.ridesservice.entity;

import jakarta.persistence.*;
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
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId; // Id del usuario (de user-service). No hay FK directa.

    @Column(name = "assigned_driver_id") // Puede ser null hasta que se asigne
    private UUID assignedDriverId; // Id del conductor (de driver-service). No hay FK directa.

    @Column(name = "assigned_vehicle_id") // Puede ser null hasta que se asigne
    private UUID assignedVehicleId; // Id del vehículo (de driver-service). No hay FK directa.

    @Column(name = "service_type", nullable = false, length = 50)
    private String serviceType; // Considerar Enum (CAR, MOTORCYCLE, DELIVERY)

    @Column(name = "ride_status", nullable = false, length = 50)
    private String rideStatus = "REQUESTED"; // Considerar Enum (REQUESTED, ASSIGNED, COMPLETED, CANCELLED...)

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