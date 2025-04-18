package com.urbango.ridesservice.client;
import com.urbango.ridesservice.dto.external.notification.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

// Apunta a la URL de notification-service
@FeignClient(name = "notification-service", url = "${services.notification.url:http://localhost:8086}/api/v1/notifications") // Asumiendo puerto 8084
public interface NotificationServiceClient {

    // Endpoint para notificar a conductores sobre un nuevo viaje disponible
    @PostMapping("/drivers/new-ride")
    ResponseEntity<Void> notifyDriversNewRide(@RequestBody NewRideNotificationRequest request); // Necesitamos un DTO para esto

    // Endpoint para notificar al usuario que su viaje fue asignado
    @PostMapping("/users/ride-assigned")
    ResponseEntity<Void> notifyUserRideAssigned(@RequestBody RideAssignedNotificationRequest request); // DTO necesario

    // Endpoint para notificar al conductor que ganó el viaje
    @PostMapping("/drivers/ride-confirmed")
    ResponseEntity<Void> notifyDriverRideConfirmed(@RequestBody RideConfirmedNotificationRequest request); // DTO necesario

    // Endpoint para notificar al usuario que el viaje terminó
    @PostMapping("/users/ride-completed")
    ResponseEntity<Void> notifyUserRideCompleted(@RequestBody RideCompletionNotificationRequest request); // DTO necesario

    // Endpoint para notificar al conductor que el viaje que intentó tomar ya fue asignado
    @PostMapping("/drivers/ride-taken")
    ResponseEntity<Void> notifyDriverRideTaken(@RequestBody RideTakenNotificationRequest request); // DTO necesario
}
