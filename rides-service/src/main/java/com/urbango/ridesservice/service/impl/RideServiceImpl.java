package com.urbango.ridesservice.service.impl;
import com.urbango.ridesservice.client.DriverServiceClient;
import com.urbango.ridesservice.client.NotificationServiceClient;
import com.urbango.ridesservice.client.UserServiceClient;
import com.urbango.ridesservice.dto.CreateRideRequestDto;
import com.urbango.ridesservice.dto.RideDto;
// Importar DTOs externos y de notificación
import com.urbango.ridesservice.dto.external.DriverDto;
import com.urbango.ridesservice.dto.external.UserDto;
import com.urbango.ridesservice.dto.external.VehicleDto;
import com.urbango.ridesservice.dto.external.notification.*; // Asumiendo un paquete para DTOs de notificación
import com.urbango.ridesservice.entity.Ride;
import com.urbango.ridesservice.enums.RideStatus;
import com.urbango.ridesservice.enums.ServiceType;
import com.urbango.ridesservice.repository.RideRepository;
import com.urbango.ridesservice.service.RideService;
import feign.FeignException; // Para capturar errores de Feign
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RideServiceImpl implements RideService {

    private final RideRepository rideRepository;
    private final UserServiceClient userServiceClient;
    private final DriverServiceClient driverServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    // Lista de estados considerados "activos" (no finalizados/cancelados)
    private static final List<RideStatus> ACTIVE_RIDE_STATUSES = List.of(
            RideStatus.REQUESTED, RideStatus.ASSIGNED
            // Añadir otros estados en curso si se implementan
    );

    @Override
    @Transactional
    public RideDto requestRide(CreateRideRequestDto requestDto) {
        log.info("Solicitud de viaje recibida para usuario {} y servicio {}", requestDto.getUserId(), requestDto.getServiceType());

        // 1. Validar que el usuario exista
        validateUserExists(requestDto.getUserId());

        // 2. Validar que el usuario no tenga otro viaje activo
        findActiveRideForUser(requestDto.getUserId()).ifPresent(activeRide -> {
            log.warn("El usuario {} ya tiene un viaje activo (ID: {})", requestDto.getUserId(), activeRide.getId());
            throw new IllegalStateException("Ya tienes un viaje activo."); // O excepción personalizada
        });

        // 3. Crear y guardar la entidad Ride inicial
        Ride newRide = new Ride();
        newRide.setUserId(requestDto.getUserId());
        newRide.setServiceType(requestDto.getServiceType());
        newRide.setRideStatus(RideStatus.REQUESTED); // Estado inicial
        newRide.setOriginDetails(requestDto.getOriginDetails());
        newRide.setDestinationDetails(requestDto.getDestinationDetails());

        Ride savedRide = rideRepository.save(newRide);
        log.info("Viaje creado con ID: {} en estado REQUESTED", savedRide.getId());

        // 4. Iniciar búsqueda de conductores (llamada asíncrona sería ideal aquí, pero por ahora síncrona)
        findAndNotifyDrivers(savedRide.getId(), savedRide.getServiceType());

        // 5. Mapear y devolver DTO
        return mapRideToDto(savedRide);
    }

    @Override
    @Transactional
    public RideDto acceptRide(UUID rideId, UUID driverId, UUID vehicleId) {
        log.info("Conductor {} intentando aceptar viaje {}", driverId, rideId);

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new EntityNotFoundException("Viaje no encontrado con ID: " + rideId));

        // 1. Validar estado del viaje (solo se puede aceptar si está REQUESTED)
        if (ride.getRideStatus() != RideStatus.REQUESTED) {
            log.warn("Intento de aceptar viaje {} que no está en estado REQUESTED (estado actual: {})", rideId, ride.getRideStatus());
            // Notificar a este conductor que ya fue tomado (si no es el que lo tiene asignado)
            if (ride.getRideStatus() == RideStatus.ASSIGNED && !driverId.equals(ride.getAssignedDriverId())) {
                notifyDriverRideTakenAsync(driverId, rideId);
            }
            throw new IllegalStateException("Este viaje ya no está disponible para ser aceptado.");
        }

        // 2. Validar conductor y vehículo (simplificado por ahora, asumimos que existen)
        // En un sistema real, verificaríamos que driverId y vehicleId son válidos y pertenecen al conductor.

        // 3. Actualizar Viaje
        ride.setAssignedDriverId(driverId);
        ride.setAssignedVehicleId(vehicleId); // Guardar vehículo
        ride.setRideStatus(RideStatus.ASSIGNED);
        ride.setAssignedAt(Instant.now());
        Ride updatedRide = rideRepository.save(ride);
        log.info("Viaje {} asignado a conductor {}", rideId, driverId);

        // 4. Actualizar estado del conductor a ON_RIDE (llamada a driver-service)
        updateDriverStatusAsync(driverId, "ON_RIDE"); // Usar el nombre del Enum

        // 5. Notificar al usuario y al conductor asignado (llamadas a notification-service)
        notifyUserRideAssignedAsync(ride.getUserId(), driverId, rideId);
        notifyDriverRideConfirmedAsync(driverId, ride.getUserId(), rideId);

        return mapRideToDto(updatedRide);
    }


    @Override
    @Transactional
    public RideDto completeRide(UUID rideId, UUID driverId) {
        log.info("Conductor {} intentando completar viaje {}", driverId, rideId);
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new EntityNotFoundException("Viaje no encontrado con ID: " + rideId));

        // 1. Validar estado y conductor asignado
        if (ride.getRideStatus() != RideStatus.ASSIGNED) { // O estados ONGOING si existen
            log.warn("Intento de completar viaje {} que no está asignado (estado actual: {})", rideId, ride.getRideStatus());
            throw new IllegalStateException("El viaje no se puede completar en el estado actual.");
        }
        if (!driverId.equals(ride.getAssignedDriverId())) {
            log.error("Intento de completar viaje {} por conductor {} que no es el asignado ({})", rideId, driverId, ride.getAssignedDriverId());
            throw new SecurityException("No estás autorizado para completar este viaje."); // O IllegalArgumentException
        }

        // 2. Actualizar Viaje
        ride.setRideStatus(RideStatus.COMPLETED);
        ride.setCompletedAt(Instant.now());
        Ride updatedRide = rideRepository.save(ride);
        log.info("Viaje {} completado por conductor {}", rideId, driverId);

        // 3. Actualizar estado del conductor a ACTIVE_AVAILABLE (llamada a driver-service)
        updateDriverStatusAsync(driverId, "ACTIVE_AVAILABLE");

        // 4. Notificar al usuario (llamada a notification-service)
        notifyUserRideCompletedAsync(ride.getUserId(), rideId);

        return mapRideToDto(updatedRide);
    }

    @Override
    @Transactional
    public RideDto cancelRide(UUID rideId, String reason) {
        log.info("Intentando cancelar viaje {} por razón: {}", rideId, reason);
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new EntityNotFoundException("Viaje no encontrado con ID: " + rideId));

        // 1. Validar si se puede cancelar (no completado/ya cancelado)
        if (EnumSet.of(RideStatus.COMPLETED, RideStatus.CANCELLED_DRIVER, RideStatus.CANCELLED_USER, RideStatus.TIMEOUT_NO_DRIVER).contains(ride.getRideStatus())) {
            log.warn("Intento de cancelar viaje {} que ya está finalizado o cancelado (estado: {})", rideId, ride.getRideStatus());
            throw new IllegalStateException("Este viaje no se puede cancelar.");
        }

        // 2. Determinar el estado de cancelación correcto
        RideStatus cancelStatus;
        // Aquí podrías tener una lógica más fina basada en la 'reason'
        if ("CANCELLED_USER".equalsIgnoreCase(reason)) {
            cancelStatus = RideStatus.CANCELLED_USER;
        } else if ("CANCELLED_DRIVER".equalsIgnoreCase(reason)) {
            cancelStatus = RideStatus.CANCELLED_DRIVER;
        } else if ("TIMEOUT_NO_DRIVER".equalsIgnoreCase(reason)) {
            cancelStatus = RideStatus.TIMEOUT_NO_DRIVER;
        } else {
            log.warn("Razón de cancelación no reconocida '{}', usando CANCELLED_USER por defecto para viaje {}", reason, rideId);
            cancelStatus = RideStatus.CANCELLED_USER; // O un estado genérico CANCELLED
        }

        UUID assignedDriverId = ride.getAssignedDriverId(); // Guardar antes de actualizar

        // 3. Actualizar Viaje
        ride.setRideStatus(cancelStatus);
        ride.setCancelledAt(Instant.now());
        ride.setCancellationReason(reason); // Guardar la razón
        Ride updatedRide = rideRepository.save(ride);
        log.info("Viaje {} cancelado con estado {}", rideId, cancelStatus);

        // 4. Si había un conductor asignado, ponerlo disponible de nuevo
        if (assignedDriverId != null && cancelStatus != RideStatus.COMPLETED) { // Evitar si se completa
            updateDriverStatusAsync(assignedDriverId, "ACTIVE_AVAILABLE");
            // Podríamos notificar al conductor sobre la cancelación también
        }

        // Podríamos notificar al usuario/conductor sobre la cancelación

        return mapRideToDto(updatedRide);
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<RideDto> getRideById(UUID rideId) {
        log.debug("Buscando viaje por ID: {}", rideId);
        return rideRepository.findById(rideId).map(this::mapRideToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RideDto> findActiveRideForUser(UUID userId) {
        log.debug("Buscando viaje activo para usuario: {}", userId);
        return rideRepository.findFirstByUserIdAndRideStatusIn(userId, ACTIVE_RIDE_STATUSES)
                .map(this::mapRideToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RideDto> findActiveRideForDriver(UUID driverId) {
        log.debug("Buscando viaje activo para conductor: {}", driverId);
        // Un conductor solo tiene un viaje activo si está ASIGNADO (o ONGOING, etc.)
        List<RideStatus> driverActiveStatuses = List.of(RideStatus.ASSIGNED); // Añadir otros si aplica
        return rideRepository.findFirstByAssignedDriverIdAndRideStatusIn(driverId, driverActiveStatuses)
                .map(this::mapRideToDto);
    }


    // --- Métodos Auxiliares ---

    private void validateUserExists(UUID userId) {
        try {
            log.debug("Validando existencia de usuario: {}", userId);
            ResponseEntity<UserDto> response = userServiceClient.getUserById(userId);
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                log.error("Usuario no encontrado o respuesta inesperada de user-service para ID: {}", userId);
                throw new EntityNotFoundException("Usuario no encontrado con ID: " + userId);
            }
            log.debug("Usuario {} validado.", userId);
        } catch (FeignException e) {
            log.error("Error al llamar a user-service para validar usuario {}: {}", userId, e.getMessage());
            if (e.status() == HttpStatus.NOT_FOUND.value()) {
                throw new EntityNotFoundException("Usuario no encontrado con ID: " + userId);
            }
            throw new RuntimeException("Error de comunicación con el servicio de usuarios.", e); // O excepción personalizada
        }
    }

    private void findAndNotifyDrivers(UUID rideId, ServiceType serviceType) {
        log.debug("Buscando conductores disponibles para viaje {} y tipo {}", rideId, serviceType);
        try {
            ResponseEntity<List<DriverDto>> response = driverServiceClient.findAvailableDrivers(serviceType.name()); // Enviar nombre del Enum
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<DriverDto> availableDrivers = response.getBody();
                if (!availableDrivers.isEmpty()) {
                    List<UUID> driverIds = availableDrivers.stream().map(DriverDto::getId).collect(Collectors.toList());
                    log.info("Encontrados {} conductores disponibles para viaje {}. Notificando...", driverIds.size(), rideId);
                    // Notificar a los conductores encontrados
                    notifyDriversNewRideAsync(rideId, driverIds);
                } else {
                    log.warn("No se encontraron conductores disponibles para viaje {} y tipo {}", rideId, serviceType);
                    // Aquí podríamos iniciar un temporizador para cancelar el viaje si nadie acepta,
                    // o poner el viaje en una cola para reintentar la búsqueda.
                    // Por ahora, solo lo registramos.
                }
            } else {
                log.error("Respuesta inesperada de driver-service al buscar conductores disponibles: Código {}", response.getStatusCode());
            }
        } catch (FeignException e) {
            log.error("Error al llamar a driver-service para buscar conductores para viaje {}: {}", rideId, e.getMessage());
            // Manejar error de comunicación (podría reintentar, etc.)
        } catch (Exception e) {
            log.error("Error inesperado durante la búsqueda y notificación de conductores para viaje {}", rideId, e);
        }
    }

    // --- Métodos Asíncronos para Llamadas a otros Servicios (Buena práctica) ---
    // Implementar estos métodos usando @Async requeriría configuración adicional
    // Por ahora, haremos llamadas síncronas pero las encapsulamos

    private void updateDriverStatusAsync(UUID driverId, String status) {
        log.debug("Enviando solicitud para actualizar estado de conductor {} a {}", driverId, status);
        try {
            Map<String, String> requestBody = Map.of("status", status);
            driverServiceClient.updateDriverStatus(driverId, requestBody);
            log.info("Solicitud de actualización de estado para conductor {} enviada.", driverId);
        } catch (FeignException e) {
            log.error("Error (Feign) al actualizar estado del conductor {}: {}", driverId, e.getMessage());
            // Manejar error: reintentar, loguear para intervención manual, etc.
        } catch (Exception e) {
            log.error("Error inesperado al actualizar estado del conductor {}", driverId, e);
        }
    }

    private void notifyDriversNewRideAsync(UUID rideId, List<UUID> driverIds) {
        log.debug("Enviando notificación de nuevo viaje {} a {} conductores", rideId, driverIds.size());
        try {
            NewRideNotificationRequest request = new NewRideNotificationRequest(rideId, driverIds); // Crear DTO
            notificationServiceClient.notifyDriversNewRide(request);
            log.info("Notificación de nuevo viaje {} enviada.", rideId);
        } catch (FeignException e) {
            log.error("Error (Feign) al notificar a conductores sobre viaje {}: {}", rideId, e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al notificar a conductores sobre viaje {}", rideId, e);
        }
    }

    /**
     * Notifica asíncronamente (o síncronamente por ahora) al usuario sobre la asignación del viaje,
     * incluyendo detalles del conductor y vehículo.
     */
    private void notifyUserRideAssignedAsync(UUID userId, UUID driverId, UUID rideId) {
        log.debug("Intentando notificar a usuario {} sobre asignación de viaje {} por conductor {}", userId, rideId, driverId);

        String driverName = "Información no disponible"; // Valor por defecto
        String driverWhatsapp = "N/A";                  // Valor por defecto
        String vehicleInfo = "Vehículo no especificado"; // Valor por defecto
        String vehicleLicensePlate = "N/A";            // Valor por defecto
        String vehicleColor = "";                     // Valor por defecto
        String vehicleModel = "";                     // Valor por defecto

        // --- Intentar obtener detalles del conductor y vehículo ---
        try {
            log.debug("Llamando a driver-service para obtener detalles del conductor {}", driverId);
            ResponseEntity<DriverDto> response = driverServiceClient.getDriverDetailsById(driverId);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                DriverDto driverDetails = response.getBody();
                driverName = driverDetails.getFullName() != null ? driverDetails.getFullName() : driverName;
                driverWhatsapp = driverDetails.getWhatsappNumber() != null ? driverDetails.getWhatsappNumber() : driverWhatsapp;

                // Intentar obtener información del primer vehículo activo (o el asignado si lo tuviéramos)
                Optional<VehicleDto> assignedVehicleOpt = driverDetails.getVehicles().stream()
                        .filter(VehicleDto::isActive) // Podríamos filtrar por el vehicleId si lo pasáramos
                        .findFirst();

                if (assignedVehicleOpt.isPresent()) {
                    VehicleDto assignedVehicle = assignedVehicleOpt.get();
                    vehicleLicensePlate = assignedVehicle.getLicensePlate() != null ? assignedVehicle.getLicensePlate() : vehicleLicensePlate;
                    vehicleColor = assignedVehicle.getColor() != null ? assignedVehicle.getColor() : vehicleColor;
                    vehicleModel = assignedVehicle.getModel() != null ? assignedVehicle.getModel() : vehicleModel;
                    vehicleInfo = String.format("%s %s %s - Placa: %s",
                            assignedVehicle.getVehicleType() != null ? assignedVehicle.getVehicleType() : "Vehículo",
                            vehicleColor,
                            vehicleModel,
                            vehicleLicensePlate).trim().replaceAll("\\s+", " "); // Formateo básico
                }
                log.debug("Detalles obtenidos para conductor {}: Nombre={}, WhatsApp={}, Vehículo={}", driverId, driverName, driverWhatsapp, vehicleInfo);
            } else {
                log.warn("No se pudieron obtener detalles completos del conductor {} desde driver-service. Código: {}", driverId, response.getStatusCode());
            }
        } catch (FeignException e) {
            log.error("Error (Feign) al obtener detalles del conductor {}: {}", driverId, e.getMessage());
            // Continuamos con valores por defecto, pero registramos el error
        } catch (Exception e) {
            log.error("Error inesperado al obtener detalles del conductor {}", driverId, e);
            // Continuamos con valores por defecto
        }


        // --- Construir y enviar la notificación ---
        try {
            RideAssignedNotificationRequest request = RideAssignedNotificationRequest.builder()
                    .userId(userId)
                    .driverId(driverId)
                    .rideId(rideId)
                    .driverName(driverName) // Usar los detalles obtenidos (o por defecto)
                    .driverWhatsapp(driverWhatsapp)
                    .vehicleInfo(vehicleInfo)
                    .vehicleLicensePlate(vehicleLicensePlate)
                    .vehicleColor(vehicleColor)
                    .vehicleModel(vehicleModel)
                    .build();

            log.debug("Enviando notificación de viaje asignado a notification-service: {}", request);
            notificationServiceClient.notifyUserRideAssigned(request);
            log.info("Notificación de viaje asignado {} enviada a usuario {}.", rideId, userId);

        } catch (FeignException e) {
            log.error("Error (Feign) al enviar notificación a usuario {} sobre viaje asignado {}: {}", userId, rideId, e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al enviar notificación a usuario {} sobre viaje asignado {}", userId, rideId, e);
        }
    }

    private void notifyDriverRideConfirmedAsync(UUID driverId, UUID userId, UUID rideId) {
        log.debug("Intentando notificar a conductor {} sobre confirmación de viaje {}", driverId, rideId);

        String userName = "Usuario"; // Valor por defecto
        String userWhatsapp = "N/A"; // Valor por defecto
        String originDetails = "";   // Valor por defecto
        String destinationDetails = ""; // Valor por defecto

        // --- Intentar obtener detalles del usuario ---
        try {
            log.debug("Llamando a user-service para obtener detalles del usuario {}", userId);
            ResponseEntity<UserDto> userResponse = userServiceClient.getUserById(userId);
            if (userResponse.getStatusCode().is2xxSuccessful() && userResponse.getBody() != null) {
                UserDto userDetails = userResponse.getBody();
                userName = userDetails.getFullName() != null ? userDetails.getFullName() : userName;
                userWhatsapp = userDetails.getWhatsappNumber() != null ? userDetails.getWhatsappNumber() : userWhatsapp;
                log.debug("Detalles obtenidos para usuario {}: Nombre={}, WhatsApp={}", userId, userName, userWhatsapp);
            } else {
                log.warn("No se pudieron obtener detalles del usuario {} desde user-service. Código: {}", userId, userResponse.getStatusCode());
            }
        } catch (FeignException e) {
            log.error("Error (Feign) al obtener detalles del usuario {}: {}", userId, e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al obtener detalles del usuario {}", userId, e);
        }

        // --- Intentar obtener detalles del viaje (origen/destino si existen) ---
        // Podríamos hacer rideRepository.findById(rideId) aquí, pero si ya tenemos la entidad Ride
        // en el método que llama a este (como en acceptRide), sería mejor pasar esos detalles.
        // Por ahora, asumimos que no los tenemos fácilmente y los dejamos vacíos o los buscamos.
        Optional<Ride> rideOpt = rideRepository.findById(rideId);
        if (rideOpt.isPresent()) {
            originDetails = rideOpt.get().getOriginDetails() != null ? rideOpt.get().getOriginDetails() : originDetails;
            destinationDetails = rideOpt.get().getDestinationDetails() != null ? rideOpt.get().getDestinationDetails() : destinationDetails;
        } else {
            log.warn("No se pudo encontrar el viaje con ID {} para obtener detalles de origen/destino.", rideId);
        }


        // --- Construir y enviar la notificación ---
        try {
            // Usar el Builder:
            RideConfirmedNotificationRequest request = RideConfirmedNotificationRequest.builder()
                    .driverId(driverId)
                    .userId(userId)
                    .rideId(rideId)
                    .userName(userName) // Usar detalles obtenidos o por defecto
                    .userWhatsapp(userWhatsapp)
                    .originDetails(originDetails)
                    .destinationDetails(destinationDetails)
                    .build();

            log.debug("Enviando notificación de confirmación de viaje a notification-service: {}", request);
            notificationServiceClient.notifyDriverRideConfirmed(request);
            log.info("Notificación de confirmación de viaje {} enviada a conductor {}.", rideId, driverId);
        } catch (FeignException e) {
            log.error("Error (Feign) al enviar confirmación a conductor {}: {}", driverId, e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al enviar confirmación a conductor {}", driverId, e);
        }
    }

    private void notifyUserRideCompletedAsync(UUID userId, UUID rideId) {
        log.debug("Notificando a usuario {} sobre finalización de viaje {}", userId, rideId);
        try {
            RideCompletionNotificationRequest request = new RideCompletionNotificationRequest(userId, rideId);
            notificationServiceClient.notifyUserRideCompleted(request);
            log.info("Notificación de viaje completado {} enviada a usuario {}.", rideId, userId);
        } catch (FeignException e) {
            log.error("Error (Feign) al notificar finalización a usuario {}: {}", userId, e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al notificar finalización a usuario {}", userId, e);
        }
    }

    private void notifyDriverRideTakenAsync(UUID driverId, UUID rideId) {
        log.debug("Notificando a conductor {} que el viaje {} ya fue tomado", driverId, rideId);
        try {
            RideTakenNotificationRequest request = new RideTakenNotificationRequest(driverId, rideId);
            notificationServiceClient.notifyDriverRideTaken(request);
            log.info("Notificación de viaje tomado {} enviada a conductor {}.", rideId, driverId);
        } catch (FeignException e) {
            log.error("Error (Feign) al notificar viaje tomado a conductor {}: {}", driverId, e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al notificar viaje tomado a conductor {}", driverId, e);
        }
    }


    // --- Mapeador Entidad Ride -> RideDto ---
    private RideDto mapRideToDto(Ride ride) {
        if (ride == null) {
            return null;
        }
        return new RideDto(
                ride.getId(),
                ride.getUserId(),
                ride.getAssignedDriverId(),
                ride.getAssignedVehicleId(),
                ride.getServiceType(), // Enum
                ride.getRideStatus(),   // Enum
                ride.getOriginDetails(),
                ride.getDestinationDetails(),
                ride.getCancellationReason(),
                ride.getCreatedAt(),
                ride.getAssignedAt(),
                ride.getCompletedAt(),
                ride.getCancelledAt(),
                ride.getUpdatedAt()
        );
    }

}
