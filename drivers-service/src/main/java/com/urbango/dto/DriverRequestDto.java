package com.urbango.dto;

public class DriverRequestDto {

    private String name;
    private String phone;
    private String vehicleType; // "moto", "carro", "domicilio"

    // Campos adicionales
    private String licenseNumber;
    private Boolean soatValid;
    private Boolean tecnomecanicaValid;
    private Boolean judicialRecordsValid;
    private String vehiclePlate;

    // Getters & Setters

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

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
