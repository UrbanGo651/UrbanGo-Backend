package com.urbango.notificationservice.service.impl;

import com.urbango.notificationservice.client.DriverServiceClient;
import com.urbango.notificationservice.client.UserServiceClient;
import com.urbango.notificationservice.dto.*; // DTOs de entrada
import com.urbango.notificationservice.dto.external.DriverDto; // DTOs externos
import com.urbango.notificationservice.dto.external.UserDto;
import com.urbango.notificationservice.service.NotificationService;
import com.urbango.notificationservice.service.WhatsAppService;
import feign.FeignException; // Importar FeignException
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity; // Importar ResponseEntity
import org.springframework.stereotype.Service;
// Quitar Autowired si usas RequiredArgsConstructor
// import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional; // Importar Optional
import java.util.UUID;

@Service
@RequiredArgsConstructor // Inyecta final fields
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final WhatsAppService whatsAppService;
    // Clientes Feign inyectados por el constructor generado por Lombok
    private final DriverServiceClient driverServiceClient;
    private final UserServiceClient userServiceClient;

    // --- Implementaciones de los métodos de notificación (sendNewRideNotification, etc.) ---
    // Estas usarán ahora las versiones reales de fetch...WhatsappNumber

    @Override
    public void sendNewRideNotification(NewRideNotificationRequest request) {
        log.info("Procesando notificación de nuevo viaje {} para {} conductores", request.getRideId(), request.getDriverIds().size());
        String rideIdShort = request.getRideId().toString().substring(0, 8);
        String messageBody = String.format(
                "¡Nuevo servicio disponible! 🚕💨\nViaje ID: %s...\nTipo: %s\nOrigen: %s\n\nResponde 'Acepto %s' para tomarlo.",
                rideIdShort,
                request.getServiceType() != null ? request.getServiceType() : "N/D",
                request.getOriginDetails() != null ? request.getOriginDetails() : "N/D",
                rideIdShort
        );

        for (UUID driverId : request.getDriverIds()) {
            // Llama al método auxiliar REAL para buscar el número
            fetchDriverWhatsappNumber(driverId).ifPresentOrElse(
                    whatsappNumber -> {
                        boolean sent = whatsAppService.sendMessage(whatsappNumber, messageBody);
                        if (!sent) log.error("Fallo al enviar notificación de nuevo viaje {} al conductor {}", request.getRideId(), driverId);
                    },
                    () -> log.warn("No se encontró número de WhatsApp para conductor {}, no se puede notificar.", driverId) // Log si Optional está vacío
            );
        }
    }

    @Override
    public void sendRideAssignedNotification(RideAssignedNotificationRequest request) {
        log.info("Procesando notificación de viaje asignado {} para usuario {}", request.getRideId(), request.getUserId());
        // Llama al método auxiliar REAL para buscar el número del usuario
        fetchUserWhatsappNumber(request.getUserId()).ifPresentOrElse(
                userWhatsapp -> {
                    // Formatea el mensaje usando los detalles pasados en el request
                    String messageBody = String.format(
                            "¡Tu viaje ha sido asignado! ✅\nConductor: %s\nVehículo: %s %s (%s)\nPlaca: %s\n\nPuedes contactarlo: wa.me/%s",
                            request.getDriverName() != null ? request.getDriverName() : "N/D",
                            request.getVehicleModel() != null ? request.getVehicleModel() : "Vehículo",
                            request.getVehicleColor() != null ? request.getVehicleColor() : "",
                            request.getVehicleType() != null ? request.getVehicleType() : "",
                            request.getVehicleLicensePlate()!= null ? request.getVehicleLicensePlate() : "N/D",
                            request.getDriverWhatsapp() != null ? request.getDriverWhatsapp().replace("+", "") : "" // Asume que driverWhatsapp viene en el request
                    );
                    whatsAppService.sendMessage(userWhatsapp, messageBody);
                },
                () -> log.warn("No se encontró número de WhatsApp para usuario {}, no se puede notificar asignación.", request.getUserId())
        );
    }

    @Override
    public void sendRideConfirmedNotification(RideConfirmedNotificationRequest request) {
        log.info("Procesando notificación de confirmación de viaje {} para conductor {}", request.getRideId(), request.getDriverId());
        // Llama al método auxiliar REAL para buscar el número del conductor
        fetchDriverWhatsappNumber(request.getDriverId()).ifPresentOrElse(
                driverWhatsapp -> {
                    String rideIdShort = request.getRideId().toString().substring(0, 8);
                    String messageBody = String.format(
                            "¡Viaje Confirmado! 👍\nUsuario: %s\nOrigen: %s\n\nContacta al usuario: wa.me/%s\n\nAl terminar, escribe 'Finalizado %s'",
                            request.getUserName() != null ? request.getUserName() : "N/D",
                            request.getOriginDetails() != null ? request.getOriginDetails() : "Recoger en punto acordado",
                            request.getUserWhatsapp() != null ? request.getUserWhatsapp().replace("+", "") : "", // Asume que userWhatsapp viene en el request
                            rideIdShort
                    );
                    whatsAppService.sendMessage(driverWhatsapp, messageBody);
                },
                () -> log.warn("No se encontró número de WhatsApp para conductor {}, no se puede notificar confirmación.", request.getDriverId())
        );
    }

    @Override
    public void sendRideCompletedNotification(RideCompletionNotificationRequest request) {
        log.info("Procesando notificación de viaje completado {} para usuario {}", request.getRideId(), request.getUserId());
        // Llama al método auxiliar REAL para buscar el número del usuario
        fetchUserWhatsappNumber(request.getUserId()).ifPresentOrElse(
                userWhatsapp -> {
                    String rideIdShort = request.getRideId().toString().substring(28); // Últimos 8
                    String messageBody = String.format(
                            "¡Tu viaje (...%s) ha finalizado! Gracias por usar UrbanGo. 👋", rideIdShort
                    );
                    whatsAppService.sendMessage(userWhatsapp, messageBody);
                },
                () -> log.warn("No se encontró número de WhatsApp para usuario {}, no se puede notificar finalización.", request.getUserId())
        );
    }

    @Override
    public void sendRideTakenNotification(RideTakenNotificationRequest request) {
        log.info("Procesando notificación de viaje ya tomado {} para conductor {}", request.getRideId(), request.getDriverId());
        // Llama al método auxiliar REAL para buscar el número del conductor
        fetchDriverWhatsappNumber(request.getDriverId()).ifPresentOrElse(
                driverWhatsapp -> {
                    String rideIdShort = request.getRideId().toString().substring(0, 8);
                    String messageBody = String.format(
                            "El servicio (...%s) que intentaste tomar ya fue asignado. ¡Gracias por responder! Sigue atento. 👀", rideIdShort
                    );
                    whatsAppService.sendMessage(driverWhatsapp, messageBody);
                },
                () -> log.warn("No se encontró número de WhatsApp para conductor {}, no se puede notificar viaje tomado.", request.getDriverId())
        );
    }


    // --- IMPLEMENTACIÓN REAL de Métodos Auxiliares para buscar números ---

    /**
     * Busca el número de WhatsApp de un conductor llamando a DriverServiceClient.
     * @param driverId ID del conductor.
     * @return Optional con el número de WhatsApp si se encuentra, Optional vacío en caso contrario o error.
     */
    private Optional<String> fetchDriverWhatsappNumber(UUID driverId) {
        log.debug("Buscando WhatsApp para conductor {}", driverId);
        try {
            // Llama al cliente Feign para obtener los detalles del conductor
            ResponseEntity<DriverDto> response = driverServiceClient.getDriverDetailsById(driverId);

            // Verifica si la respuesta es exitosa, tiene cuerpo y el número no es nulo/vacío
            if (response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null
                    && response.getBody().getWhatsappNumber() != null
                    && !response.getBody().getWhatsappNumber().isBlank()) {

                String whatsappNumber = response.getBody().getWhatsappNumber();
                log.debug("WhatsApp encontrado para conductor {}: {}", driverId, whatsappNumber);
                return Optional.of(whatsappNumber);
            } else {
                // Loguea si la respuesta no fue exitosa o faltan datos
                log.warn("No se pudo obtener WhatsApp para conductor {} desde driver-service. Código: {} / Cuerpo nulo o WhatsApp vacío: {}",
                        driverId, response.getStatusCode(), response.getBody() == null || response.getBody().getWhatsappNumber() == null);
                return Optional.empty();
            }
        } catch (FeignException e) {
            // Captura errores específicos de Feign (red, 404, 500 del otro servicio)
            log.error("Error (Feign) al obtener WhatsApp para conductor {}: Status={}, Body={}", driverId, e.status(), e.contentUTF8(), e);
            return Optional.empty();
        } catch (Exception e) {
            // Captura cualquier otro error inesperado
            log.error("Error inesperado al obtener WhatsApp para conductor {}", driverId, e);
            return Optional.empty();
        }
    }

    /**
     * Busca el número de WhatsApp de un usuario llamando a UserServiceClient.
     * @param userId ID del usuario.
     * @return Optional con el número de WhatsApp si se encuentra, Optional vacío en caso contrario o error.
     */
    private Optional<String> fetchUserWhatsappNumber(UUID userId) {
        log.debug("Buscando WhatsApp para usuario {}", userId);
        try {
            // Llama al cliente Feign para obtener los detalles del usuario
            ResponseEntity<UserDto> response = userServiceClient.getUserById(userId);

            // Verifica si la respuesta es exitosa, tiene cuerpo y el número no es nulo/vacío
            if (response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null
                    && response.getBody().getWhatsappNumber() != null
                    && !response.getBody().getWhatsappNumber().isBlank()) {

                String whatsappNumber = response.getBody().getWhatsappNumber();
                log.debug("WhatsApp encontrado para usuario {}: {}", userId, whatsappNumber);
                return Optional.of(whatsappNumber);
            } else {
                log.warn("No se pudo obtener WhatsApp para usuario {} desde user-service. Código: {} / Cuerpo nulo o WhatsApp vacío: {}",
                        userId, response.getStatusCode(), response.getBody() == null || response.getBody().getWhatsappNumber() == null);
                return Optional.empty();
            }
        } catch (FeignException e) {
            log.error("Error (Feign) al obtener WhatsApp para usuario {}: Status={}, Body={}", userId, e.status(), e.contentUTF8(), e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error inesperado al obtener WhatsApp para usuario {}", userId, e);
            return Optional.empty();
        }
    }
}
