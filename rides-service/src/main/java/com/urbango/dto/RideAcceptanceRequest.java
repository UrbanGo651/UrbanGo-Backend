package com.urbango.dto;

public class RideAcceptanceRequest {
    private Long rideId;
    private Long driverId; // En este ejemplo, simularemos que es el teléfono o identificador

    public Long getRideId() {
        return rideId;
    }

    public void setRideId(Long rideId) {
        this.rideId = rideId;
    }

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }
}
