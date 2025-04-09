package com.urbango.dto;


public class GroupNotificationRequestDto {
    private String groupType; // "carro", "moto", "domicilio"
    private String message;

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
