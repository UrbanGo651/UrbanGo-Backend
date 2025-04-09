package com.urbango.dto;

public class NotificationRequestDto {

    private String message;
    private String groupType; // "carro", "moto" o "domicilio"

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }
}
