package com.urbango.dto;

public class NotificationRequestDto {
    private String receiverType; // "user" o "driver"
    private String receiverId;   // Número de teléfono o identificador WhatsApp (por ejemplo, "3001234567")
    private String message;

    public String getReceiverType() {
        return receiverType;
    }

    public void setReceiverType(String receiverType) {
        this.receiverType = receiverType;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
