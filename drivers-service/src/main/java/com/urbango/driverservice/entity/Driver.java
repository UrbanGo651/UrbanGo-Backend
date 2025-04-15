package com.urbango.driverservice.entity;

import com.urbango.driverservice.enums.DriverStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "drivers") // Schema definido en application-dev.yml
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "whatsapp_number", unique = true, nullable = false, length = 25)
    private String whatsappNumber;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING) // Mapear a VARCHAR en BD
    @Column(name = "driver_status", nullable = false, length = 50)
    private DriverStatus driverStatus = DriverStatus.PENDING_APPROVAL; // Usar Enum para valor por defecto

    @Column(name = "approved_by") // Puede ser null inicialmente
    private UUID approvedBy;

    @Column(name = "approval_timestamp") // Puede ser null inicialmente
    private Instant approvalTimestamp;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // Relación One-to-Many con Vehículos
    // mappedBy="driver" indica que la FK está en la entidad Vehicle, en el campo 'driver'
    // CascadeType.ALL: Operaciones (persist, merge, remove, etc.) se propagan a los vehículos.
    // orphanRemoval=true: Si quitas un vehículo de esta lista, será eliminado de la BD.
    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Vehicle> vehicles = new ArrayList<>();

    // Relación One-to-Many con Documentos
    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DriverDocument> documents = new ArrayList<>();

    // Métodos de ayuda para gestionar las relaciones bidireccionales (opcional pero buena práctica)
    public void addVehicle(Vehicle vehicle) {
        vehicles.add(vehicle);
        vehicle.setDriver(this);
    }

    public void removeVehicle(Vehicle vehicle) {
        vehicles.remove(vehicle);
        vehicle.setDriver(null);
    }

    public void addDocument(DriverDocument document) {
        documents.add(document);
        document.setDriver(this);
    }

    public void removeDocument(DriverDocument document) {
        documents.remove(document);
        document.setDriver(null);
    }
}