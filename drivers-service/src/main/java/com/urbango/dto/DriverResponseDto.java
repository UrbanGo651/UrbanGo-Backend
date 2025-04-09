package com.urbango.dto;

import java.time.LocalDateTime;

public class DriverResponseDto {
    private Long id;
    private String name;
    private String phone;
    private String vehicleType;
    private String status;
    private LocalDateTime createdAt;

    // Campos adicionales
    private String licenseNumber;
    private Boolean soatValid;
    private Boolean tecnomecanicaValid;
    private Boolean judicialRecordsValid;
    private String vehiclePlate;

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

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
}
