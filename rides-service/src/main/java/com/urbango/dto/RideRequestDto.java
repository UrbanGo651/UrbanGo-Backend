package com.urbango.dto;


public class RideRequestDto {

    private String type; // "moto", "carro" o "domicilio"

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
