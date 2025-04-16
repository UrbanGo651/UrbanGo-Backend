package com.urbango.notificationservice.service;

import com.urbango.notificationservice.dto.*; // Importar DTOs de request

public interface NotificationService {
    void sendNewRideNotification(NewRideNotificationRequest request);
    void sendRideAssignedNotification(RideAssignedNotificationRequest request);
    void sendRideConfirmedNotification(RideConfirmedNotificationRequest request);
    void sendRideCompletedNotification(RideCompletionNotificationRequest request);
    void sendRideTakenNotification(RideTakenNotificationRequest request);
}
