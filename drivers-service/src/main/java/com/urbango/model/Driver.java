package com.urbango.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "drivers")
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column(name = "vehicle_type", nullable = false)
    private String vehicleType; // Ejemplo: "moto", "carro", "domicilio"

    @Column(nullable = false)
    private String status; // "disponible" u "ocupado"

    // Campos adicionales para la validación del conductor:
    @Column(name = "license_number", nullable = false)
    private String licenseNumber;

    @Column(name = "soat_valid", nullable = false)
    private Boolean soatValid;

    @Column(name = "tecnomecanica_valid", nullable = false)
    private Boolean tecnomecanicaValid;

    @Column(name = "judicial_records_valid", nullable = false)
    private Boolean judicialRecordsValid;

    @Column(name = "vehicle_plate", nullable = false)
    private String vehiclePlate;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters & Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public Boolean getSoatValid() { return soatValid; }
    public void setSoatValid(Boolean soatValid) { this.soatValid = soatValid; }

    public Boolean getTecnomecanicaValid() { return tecnomecanicaValid; }
    public void setTecnomecanicaValid(Boolean tecnomecanicaValid) { this.tecnomecanicaValid = tecnomecanicaValid; }

    public Boolean getJudicialRecordsValid() { return judicialRecordsValid; }
    public void setJudicialRecordsValid(Boolean judicialRecordsValid) { this.judicialRecordsValid = judicialRecordsValid; }

    public String getVehiclePlate() { return vehiclePlate; }
    public void setVehiclePlate(String vehiclePlate) { this.vehiclePlate = vehiclePlate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
