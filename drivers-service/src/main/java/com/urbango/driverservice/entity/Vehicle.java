package com.urbango.driverservice.entity;

import com.urbango.driverservice.enums.VehicleType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode; // Para evitar problemas con relaciones bidireccionales en equals/hashCode
import lombok.ToString; // Para evitar problemas con relaciones bidireccionales en toString
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vehicles") // Schema definido en application-dev.yml
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false, nullable = false)
    private UUID id;

    // Relación Many-to-One con Conductor
    // FetchType.LAZY: No carga el conductor a menos que se acceda explícitamente.
    @ToString.Exclude // Evita recursión infinita en toString generado por Lombok
    @EqualsAndHashCode.Exclude // Evita recursión infinita en equals/hashCode generado por Lombok
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false) // Columna FK en esta tabla
    private Driver driver;

    @Column(name = "license_plate", unique = true, nullable = false, length = 10)
    private String licensePlate;

    @Enumerated(EnumType.STRING) // Mapear a VARCHAR
    @Column(name = "vehicle_type", nullable = false, length = 50)
    private VehicleType vehicleType;

    @Column(length = 100)
    private String model;

    @Column(length = 50)
    private String color;

    @Column(name = "soat_expiry_date")
    private LocalDate soatExpiryDate;

    @Column(name = "techno_expiry_date")
    private LocalDate technoExpiryDate;

    @Column(name = "is_active")
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}