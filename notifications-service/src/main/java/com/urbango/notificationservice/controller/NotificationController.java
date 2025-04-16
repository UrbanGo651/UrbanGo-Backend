package com.urbango.notificationservice.controller;

import com.urbango.notificationservice.dto.*; // Importar DTOs
import com.urbango.notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/drivers/new-ride")
    public ResponseEntity<Void> notifyDriversNewRide(@Valid @RequestBody NewRideNotificationRequest request) {
        log.info("Recibida solicitud para notificar nuevo viaje: {}", request.getRideId());
        try {
            notificationService.sendNewRideNotification(request);
            return ResponseEntity.accepted().build(); // 202 Accepted indica que la solicitud se procesará
        } catch (Exception e) {
            log.error("Error al procesar notificación de nuevo viaje {}: {}", request.getRideId(), e.getMessage(), e);
            // Considerar devolver un error 500 si la aceptación falló críticamente
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/users/ride-assigned")
    public ResponseEntity<Void> notifyUserRideAssigned(@Valid @RequestBody RideAssignedNotificationRequest request) {
        log.info("Recibida solicitud para notificar viaje asignado: {}", request.getRideId());
        try {
            notificationService.sendRideAssignedNotification(request);
            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            log.error("Error al procesar notificación de viaje asignado {}: {}", request.getRideId(), e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/drivers/ride-confirmed")
    public ResponseEntity<Void> notifyDriverRideConfirmed(@Valid @RequestBody RideConfirmedNotificationRequest request) {
        log.info("Recibida solicitud para notificar confirmación de viaje: {}", request.getRideId());
        try {
            notificationService.sendRideConfirmedNotification(request);
            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            log.error("Error al procesar notificación de confirmación de viaje {}: {}", request.getRideId(), e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/users/ride-completed")
    public ResponseEntity<Void> notifyUserRideCompleted(@Valid @RequestBody RideCompletionNotificationRequest request) {
        log.info("Recibida solicitud para notificar viaje completado: {}", request.getRideId());
        try {
            notificationService.sendRideCompletedNotification(request);
            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            log.error("Error al procesar notificación de viaje completado {}: {}", request.getRideId(), e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/drivers/ride-taken")
    public ResponseEntity<Void> notifyDriverRideTaken(@Valid @RequestBody RideTakenNotificationRequest request) {
        log.info("Recibida solicitud para notificar viaje ya tomado: {}", request.getRideId());
        try {
            notificationService.sendRideTakenNotification(request);
            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            log.error("Error al procesar notificación de viaje ya tomado {}: {}", request.getRideId(), e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
