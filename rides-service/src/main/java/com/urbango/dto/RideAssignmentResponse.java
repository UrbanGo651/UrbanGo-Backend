package com.urbango.dto;

public class RideAssignmentResponse {
    private Long rideId;
    private String driverId;
    private String driverName;
    private String vehiclePlate;
    private String vehicleModel;
    private String driverWhatsAppLink;

    public Long getRideId() {
        return rideId;
    }

    public void setRideId(Long rideId) {
        this.rideId = rideId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getVehiclePlate() {
        return vehiclePlate;
    }

    public void setVehiclePlate(String vehiclePlate) {
        this.vehiclePlate = vehiclePlate;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public String getDriverWhatsAppLink() {
        return driverWhatsAppLink;
    }

    public void setDriverWhatsAppLink(String driverWhatsAppLink) {
        this.driverWhatsAppLink = driverWhatsAppLink;
    }
}
