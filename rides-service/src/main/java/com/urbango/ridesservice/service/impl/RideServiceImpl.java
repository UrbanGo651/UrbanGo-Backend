package com.urbango.ridesservice.service.impl; // O tu paquete de implementación

import com.urbango.ridesservice.client.DriverServiceClient;
import com.urbango.ridesservice.client.NotificationServiceClient;
import com.urbango.ridesservice.client.UserServiceClient;
import com.urbango.ridesservice.dto.CreateRideRequestDto;
import com.urbango.ridesservice.dto.RideDto;
// Importar DTOs externos y de notificación (asegúrate de tener estas clases en sus paquetes)
import com.urbango.ridesservice.dto.external.DriverDto;
import com.urbango.ridesservice.dto.external.UserDto;
import com.urbango.ridesservice.dto.external.VehicleDto; // Necesario para notificación
import com.urbango.ridesservice.dto.external.notification.*;
import com.urbango.ridesservice.entity.Ride;
import com.urbango.ridesservice.enums.DriverStatus; // Enum de Driver (necesitamos replicarlo aquí o usar módulo common)
import com.urbango.ridesservice.enums.RideStatus;
import com.urbango.ridesservice.enums.ServiceType;
import com.urbango.ridesservice.repository.RideRepository;
import com.urbango.ridesservice.service.RideService;
import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*; // Importar Comparator, Set, EnumSet
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RideServiceImpl implements RideService {

    private final RideRepository rideRepository;
    private final UserServiceClient userServiceClient;
    private final DriverServiceClient driverServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    // Estados considerados "activos" para verificar si un usuario/conductor ya tiene uno
    private static final Set<RideStatus> ACTIVE_RIDE_STATUSES_USER = EnumSet.of(
            RideStatus.REQUESTED, RideStatus.ASSIGNED
            // Añadir EN_ROUTE_TO_PICKUP, AT_PICKUP, ONGOING si se implementan
    );
    private static final Set<RideStatus> ACTIVE_RIDE_STATUSES_DRIVER = EnumSet.of(
            RideStatus.ASSIGNED
            // Añadir EN_ROUTE_TO_PICKUP, AT_PICKUP, ONGOING si se implementan
    );
    // Estados finales que impiden cancelación
    private static final Set<RideStatus> FINAL_RIDE_STATUSES = EnumSet.of(
            RideStatus.COMPLETED, RideStatus.CANCELLED_DRIVER, RideStatus.CANCELLED_USER, RideStatus.TIMEOUT_NO_DRIVER
    );

    @Override
    @Transactional
    public RideDto requestRide(CreateRideRequestDto requestDto) {
        log.info("Solicitud de viaje recibida para usuario {} y servicio {}", requestDto.getUserId(), requestDto.getServiceType());

        validateUserExists(requestDto.getUserId()); // Lanza excepción si no existe

        findActiveRideForUserInternal(requestDto.getUserId()).ifPresent(activeRide -> { // Usa método interno
            log.warn("El usuario {} ya tiene un viaje activo (ID: {})", requestDto.getUserId(), activeRide.getId());
            throw new IllegalStateException("Ya tienes un viaje activo.");
        });

        Ride newRide = new Ride();
        newRide.setUserId(requestDto.getUserId());
        newRide.setServiceType(requestDto.getServiceType());
        newRide.setRideStatus(RideStatus.REQUESTED); // Estado inicial
        newRide.setOriginDetails(requestDto.getOriginDetails());
        newRide.setDestinationDetails(requestDto.getDestinationDetails());
        // El shortId se genera automáticamente con @PrePersist en la entidad Ride

        Ride savedRide = rideRepository.save(newRide);
        log.info("Viaje creado con ID: {} y ShortID: {} en estado REQUESTED", savedRide.getId(), savedRide.getShortId());

        // Iniciar búsqueda de conductores (asíncrono idealmente)
        findAndNotifyDrivers(savedRide); // Pasar la entidad completa

        return mapRideToDto(savedRide);
    }

    @Override
    @Transactional
    public RideDto acceptRide(UUID rideId, UUID driverId, UUID vehicleId) {
        log.info("Conductor {} intentando aceptar viaje {}", driverId, rideId);

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new EntityNotFoundException("Viaje no encontrado con ID: " + rideId));

        if (ride.getRideStatus() != RideStatus.REQUESTED) {
            log.warn("Intento de aceptar viaje {} que no está en estado REQUESTED (estado actual: {})", rideId, ride.getRideStatus());
            if (ride.getRideStatus() == RideStatus.ASSIGNED && !driverId.equals(ride.getAssignedDriverId())) {
                notifyDriverRideTakenAsync(driverId, rideId); // Notificar al conductor que intentó tarde
            }
            throw new IllegalStateException("Este viaje ya no está disponible para ser aceptado.");
        }

        // Validar conductor y vehículo (podría ser más extenso)
        Optional<DriverDto> driverOpt = validateDriverCanAccept(driverId, vehicleId, ride.getServiceType());
        if (driverOpt.isEmpty()) {
            // La validación ya lanzó la excepción o logueó el error
            // Lanzamos una excepción genérica aquí para detener el flujo si la validación no lo hizo
            throw new IllegalStateException("El conductor o vehículo no son válidos para aceptar este viaje.");
        }
        DriverDto driverDetails = driverOpt.get(); // Tenemos los detalles del conductor validados

        // Actualizar Viaje
        ride.setAssignedDriverId(driverId);
        ride.setAssignedVehicleId(vehicleId);
        ride.setRideStatus(RideStatus.ASSIGNED);
        ride.setAssignedAt(Instant.now());
        Ride updatedRide = rideRepository.save(ride);
        log.info("Viaje {} asignado a conductor {}", rideId, driverId);

        // Actualizar estado del conductor a ON_RIDE
        updateDriverStatusAsync(driverId, DriverStatus.ON_RIDE); // Usar Enum

        // Notificar al usuario y al conductor
        notifyUserRideAssignedAsync(ride.getUserId(), driverDetails, vehicleId, rideId); // Pasar DriverDto
        notifyDriverRideConfirmedAsync(driverId, ride.getUserId(), rideId, ride.getOriginDetails(), ride.getDestinationDetails()); // Pasar detalles

        return mapRideToDto(updatedRide);
    }


    @Override
    @Transactional
    public RideDto completeRide(UUID rideId, UUID driverId) {
        log.info("Conductor {} intentando completar viaje {}", driverId, rideId);
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new EntityNotFoundException("Viaje no encontrado con ID: " + rideId));

        // Validar estado y conductor
        if (!EnumSet.of(RideStatus.ASSIGNED /*, RideStatus.ONGOING, etc */).contains(ride.getRideStatus())) {
            log.warn("Intento de completar viaje {} que no está asignado/en curso (estado actual: {})", rideId, ride.getRideStatus());
            throw new IllegalStateException("El viaje no se puede completar en el estado actual.");
        }
        if (!driverId.equals(ride.getAssignedDriverId())) {
            log.error("Intento de completar viaje {} por conductor {} que no es el asignado ({})", rideId, driverId, ride.getAssignedDriverId());
            throw new SecurityException("No estás autorizado para completar este viaje.");
        }

        // Actualizar Viaje
        ride.setRideStatus(RideStatus.COMPLETED);
        ride.setCompletedAt(Instant.now());
        Ride updatedRide = rideRepository.save(ride);
        log.info("Viaje {} completado por conductor {}", rideId, driverId);

        // Actualizar estado del conductor a ACTIVE_AVAILABLE
        updateDriverStatusAsync(driverId, DriverStatus.ACTIVE_AVAILABLE); // Usar Enum

        // Notificar al usuario
        notifyUserRideCompletedAsync(ride.getUserId(), rideId);

        return mapRideToDto(updatedRide);
    }

    @Override
    @Transactional
    public RideDto cancelRide(UUID rideId, String reason) {
        log.info("Intentando cancelar viaje {} por razón: {}", rideId, reason);
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new EntityNotFoundException("Viaje no encontrado con ID: " + rideId));

        if (FINAL_RIDE_STATUSES.contains(ride.getRideStatus())) {
            log.warn("Intento de cancelar viaje {} que ya está finalizado o cancelado (estado: {})", rideId, ride.getRideStatus());
            throw new IllegalStateException("Este viaje no se puede cancelar.");
        }

        RideStatus cancelStatus = determineCancelStatus(reason); // Usar método auxiliar
        UUID assignedDriverId = ride.getAssignedDriverId();

        // Actualizar Viaje
        ride.setRideStatus(cancelStatus);
        ride.setCancelledAt(Instant.now());
        ride.setCancellationReason(reason);
        Ride updatedRide = rideRepository.save(ride);
        log.info("Viaje {} cancelado con estado {}", rideId, cancelStatus);

        // Poner al conductor disponible si estaba asignado
        if (assignedDriverId != null) {
            updateDriverStatusAsync(assignedDriverId, DriverStatus.ACTIVE_AVAILABLE);
            // TODO: Notificar al conductor/usuario sobre la cancelación si es necesario
        }

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
    public Optional<RideDto> findRideByShortId(String shortId) {
        log.debug("Buscando viaje por ID corto (columna dedicada): {}", shortId);
        // Usar el nuevo método eficiente del repositorio
        return rideRepository.findByShortId(shortId)
                .map(this::mapRideToDto);
    }

    // Método público expuesto por la interfaz
    @Override
    @Transactional(readOnly = true)
    public Optional<RideDto> findActiveRideForUser(UUID userId) {
        log.debug("Buscando viaje activo para usuario: {}", userId);
        return findActiveRideForUserInternal(userId).map(this::mapRideToDto);
    }

    // Método público expuesto por la interfaz
    @Override
    @Transactional(readOnly = true)
    public Optional<RideDto> findActiveRideForDriver(UUID driverId) {
        log.debug("Buscando viaje activo para conductor: {}", driverId);
        return rideRepository.findFirstByAssignedDriverIdAndRideStatusIn(driverId, List.copyOf(ACTIVE_RIDE_STATUSES_DRIVER))
                .map(this::mapRideToDto);
    }

    // --- Métodos Auxiliares Internos ---

    // Método interno para buscar viaje activo de usuario (devuelve Entidad)
    private Optional<Ride> findActiveRideForUserInternal(UUID userId) {
        return rideRepository.findFirstByUserIdAndRideStatusIn(userId, List.copyOf(ACTIVE_RIDE_STATUSES_USER));
    }

    private void validateUserExists(UUID userId) {
        try {
            log.debug("(RideService) Validando existencia de usuario: {}", userId);
            ResponseEntity<UserDto> response = userServiceClient.getUserById(userId); // Llama al cliente Feign
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error("Usuario no encontrado o respuesta inválida de user-service para ID: {}", userId);
                throw new EntityNotFoundException("Usuario solicitante no encontrado con ID: " + userId);
            }
            log.debug("Usuario {} validado OK.", userId);
        } catch (FeignException e) {
            log.error("Error (Feign) al validar usuario {}: Status={}, Body={}", userId, e.status(), e.contentUTF8(), e);
            if (e.status() == HttpStatus.NOT_FOUND.value()) {
                throw new EntityNotFoundException("Usuario solicitante no encontrado con ID: " + userId);
            }
            throw new RuntimeException("Error de comunicación al validar usuario.", e);
        }
    }

    private Optional<DriverDto> validateDriverCanAccept(UUID driverId, UUID vehicleId, ServiceType requiredServiceType) {
        log.debug("Validando si conductor {} con vehículo {} puede aceptar servicio tipo {}", driverId, vehicleId, requiredServiceType);
        try {
            ResponseEntity<DriverDto> response = driverServiceClient.getDriverDetailsById(driverId);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error("No se pudieron obtener detalles del conductor {} desde driver-service. Código: {}", driverId, response.getStatusCode());
                throw new IllegalArgumentException("No se pudo verificar la información del conductor.");
            }
            DriverDto driver = response.getBody();

            // Verificar estado del conductor (debe estar AVAILABLE para aceptar)
            // IMPORTANTE: Comparar con el ENUM o su representación String
            if (!DriverStatus.ACTIVE_AVAILABLE.name().equalsIgnoreCase(driver.getDriverStatus())) {
                log.warn("Conductor {} no está disponible para aceptar. Estado actual: {}", driverId, driver.getDriverStatus());
                // Notificar que ya fue tomado, ya que otro conductor pudo ser más rápido en aceptar Y este conductor cambió su estado
                notifyDriverRideTakenAsync(driverId, null); // Pasamos rideId null porque no lo tenemos fácilmente aquí
                throw new IllegalStateException("Ya no estás disponible para aceptar servicios.");
            }

            // Verificar si el vehículo especificado pertenece al conductor y es del tipo correcto y está activo
            Optional<VehicleDto> vehicleOpt = driver.getVehicles().stream()
                    .filter(v -> vehicleId.equals(v.getId()))
                    .findFirst();

            if (vehicleOpt.isEmpty()) {
                log.error("El vehículo {} no pertenece al conductor {}", vehicleId, driverId);
                throw new IllegalArgumentException("El vehículo especificado no te pertenece.");
            }

            VehicleDto vehicle = vehicleOpt.get();
            if (!vehicle.isActive()) {
                log.error("El vehículo {} del conductor {} no está activo", vehicleId, driverId);
                throw new IllegalArgumentException("El vehículo seleccionado no está activo.");
            }

            // Comparar tipo de servicio requerido con tipo de vehículo (ajustar si es necesario)
            boolean typeMatch = false;
            if (requiredServiceType == ServiceType.CAR && "CAR".equalsIgnoreCase(vehicle.getVehicleType())) typeMatch = true;
            if (requiredServiceType == ServiceType.MOTORCYCLE && "MOTORCYCLE".equalsIgnoreCase(vehicle.getVehicleType())) typeMatch = true;
            if (requiredServiceType == ServiceType.DELIVERY && List.of("MOTORCYCLE", "BICYCLE", "CAR").contains(vehicle.getVehicleType().toUpperCase())) typeMatch = true; // Delivery puede ser en varios tipos

            if (!typeMatch) {
                log.error("Tipo de vehículo {} no coincide con servicio requerido {} para conductor {}", vehicle.getVehicleType(), requiredServiceType, driverId);
                throw new IllegalArgumentException("Tu vehículo no es apto para este tipo de servicio.");
            }

            log.info("Conductor {} y vehículo {} validados para aceptar servicio {}", driverId, vehicleId, requiredServiceType);
            return Optional.of(driver); // Devuelve el DriverDto validado

        } catch (FeignException e) {
            log.error("Error (Feign) al validar conductor/vehículo {}: {}", driverId, e.getMessage());
            throw new RuntimeException("Error de comunicación al validar conductor.", e);
        }
    }


    private void findAndNotifyDrivers(Ride ride) { // Recibe la entidad Ride
        log.debug("Buscando conductores disponibles para viaje {} (ShortID: {}) tipo {}", ride.getId(), ride.getShortId(), ride.getServiceType());
        try {
            // Llama a driver-service para obtener conductores disponibles para el tipo de servicio
            ResponseEntity<List<DriverDto>> response = driverServiceClient.findAvailableDrivers(ride.getServiceType().name());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<DriverDto> availableDrivers = response.getBody();
                if (!availableDrivers.isEmpty()) {
                    List<UUID> driverIds = availableDrivers.stream().map(DriverDto::getId).collect(Collectors.toList());
                    log.info("Encontrados {} conductores disponibles para viaje {}. Notificando...", driverIds.size(), ride.getId());

                    // Construir la solicitud de notificación
                    NewRideNotificationRequest notificationRequest = new NewRideNotificationRequest(
                            ride.getId(),
                            driverIds,
                            ride.getServiceType().name(),
                            ride.getOriginDetails()
                           //ride.getDestinationDetails() // Incluir destino si el DTO lo tiene
                    );
                    notifyDriversNewRideAsync(notificationRequest); // Llamar al método async simulado

                } else {
                    log.warn("No se encontraron conductores disponibles para viaje {} y tipo {}", ride.getId(), ride.getServiceType());
                    // TODO: Implementar lógica de timeout o reintento si no se encuentran conductores.
                    // Por ejemplo, cancelar el viaje después de X minutos:
                    // scheduleCancellation(ride.getId(), "TIMEOUT_NO_DRIVER", Duration.ofMinutes(5));
                }
            } else {
                log.error("Respuesta inesperada de driver-service al buscar conductores: Código {}", response.getStatusCode());
            }
        } catch (FeignException e) {
            log.error("Error (Feign) al buscar conductores para viaje {}: {}", ride.getId(), e.getMessage());
            // Podrías intentar cancelar el viaje aquí también si falla la comunicación
            // cancelRide(ride.getId(), "ERROR_FINDING_DRIVERS");
        } catch (Exception e) {
            log.error("Error inesperado buscando/notificando conductores para viaje {}: {}", ride.getId(), e.getMessage(), e);
        }
    }

    private RideStatus determineCancelStatus(String reason) {
        if ("CANCELLED_USER".equalsIgnoreCase(reason)) return RideStatus.CANCELLED_USER;
        if ("CANCELLED_DRIVER".equalsIgnoreCase(reason)) return RideStatus.CANCELLED_DRIVER;
        if ("TIMEOUT_NO_DRIVER".equalsIgnoreCase(reason)) return RideStatus.TIMEOUT_NO_DRIVER;
        // Añadir más razones si es necesario
        return RideStatus.CANCELLED_USER; // Default si la razón no es reconocida
    }

    // --- Métodos Asíncronos Simulados para Llamadas a otros Servicios ---

    private void updateDriverStatusAsync(UUID driverId, DriverStatus status) { // Recibe Enum
        log.debug("Enviando solicitud para actualizar estado de conductor {} a {}", driverId, status);
        try {
            Map<String, String> requestBody = Map.of("status", status.name()); // Enviar nombre del Enum
            driverServiceClient.updateDriverStatus(driverId, requestBody);
            log.info("Solicitud de actualización de estado {} para conductor {} enviada.", status, driverId);
        } catch (Exception e) {
            log.error("Fallo al enviar actualización de estado para conductor {}: {}", driverId, e.getMessage(), e);
            // Considerar mecanismo de reintento o log para acción manual
        }
    }

    private void notifyDriversNewRideAsync(NewRideNotificationRequest request) {
        log.debug("Enviando notificación de nuevo viaje {} a {} conductores", request.getRideId(), request.getDriverIds().size());
        try {
            notificationServiceClient.notifyDriversNewRide(request);
            log.info("Notificación de nuevo viaje {} enviada.", request.getRideId());
        } catch (Exception e) {
            log.error("Fallo al enviar notificación de nuevo viaje {}: {}", request.getRideId(), e.getMessage(), e);
        }
    }

    // Modificado para recibir DriverDto y no necesitar buscar info de nuevo
    private void notifyUserRideAssignedAsync(UUID userId, DriverDto driverDetails, UUID vehicleId, UUID rideId) {
        log.debug("Notificando a usuario {} sobre asignación de viaje {}", userId, rideId);
        try {
            VehicleDto assignedVehicle = driverDetails.getVehicles().stream()
                    .filter(v -> vehicleId.equals(v.getId()))
                    .findFirst().orElse(null); // Encuentra el vehículo usado

            RideAssignedNotificationRequest request = RideAssignedNotificationRequest.builder()
                    .userId(userId)
                    .driverId(driverDetails.getId())
                    .rideId(rideId)
                    .driverName(driverDetails.getFullName())
                    .driverWhatsapp(driverDetails.getWhatsappNumber())
                    .vehicleType(assignedVehicle != null ? assignedVehicle.getVehicleType() : null) // Usar String o replicar Enum VehicleType aquí
                    .vehicleModel(assignedVehicle != null ? assignedVehicle.getModel() : null)
                    .vehicleColor(assignedVehicle != null ? assignedVehicle.getColor() : null)
                    .vehicleLicensePlate(assignedVehicle != null ? assignedVehicle.getLicensePlate() : null)
                    .build();

            notificationServiceClient.notifyUserRideAssigned(request);
            log.info("Notificación de viaje asignado {} enviada a usuario {}.", rideId, userId);
        } catch (Exception e) {
            log.error("Fallo al enviar notificación de viaje asignado {} a usuario {}: {}", rideId, userId, e.getMessage(), e);
        }
    }

    // Modificado para recibir más detalles y no buscarlos de nuevo
    private void notifyDriverRideConfirmedAsync(UUID driverId, UUID userId, UUID rideId, String origin, String destination) {
        log.debug("Notificando a conductor {} sobre confirmación de viaje {}", driverId, rideId);
        try {
            // TODO: Necesitaríamos llamar a userServiceClient para obtener nombre y WhatsApp del usuario
            String userName = "Usuario"; // Placeholder
            String userWhatsapp = null; // Placeholder

            RideConfirmedNotificationRequest request = RideConfirmedNotificationRequest.builder()
                    .driverId(driverId)
                    .userId(userId)
                    .rideId(rideId)
                    .userName(userName)
                    .userWhatsapp(userWhatsapp)
                    .originDetails(origin)
                    .destinationDetails(destination)
                    .build();
            notificationServiceClient.notifyDriverRideConfirmed(request);
            log.info("Notificación de confirmación de viaje {} enviada a conductor {}.", rideId, driverId);
        } catch (Exception e) {
            log.error("Fallo al enviar notificación de confirmación a conductor {}: {}", driverId, e.getMessage(), e);
        }
    }

    private void notifyUserRideCompletedAsync(UUID userId, UUID rideId) {
        log.debug("Notificando a usuario {} sobre finalización de viaje {}", userId, rideId);
        try {
            RideCompletionNotificationRequest request = new RideCompletionNotificationRequest(userId, rideId);
            notificationServiceClient.notifyUserRideCompleted(request);
            log.info("Notificación de viaje completado {} enviada a usuario {}.", rideId, userId);
        } catch (Exception e) {
            log.error("Fallo al enviar notificación de finalización a usuario {}: {}", userId, e.getMessage(), e);
        }
    }

    private void notifyDriverRideTakenAsync(UUID driverId, UUID rideId) {
        // rideId puede ser null si el error ocurre antes de identificar el viaje
        log.debug("Notificando a conductor {} que el viaje {} ya fue tomado", driverId, rideId != null ? rideId : "desconocido");
        try {
            RideTakenNotificationRequest request = new RideTakenNotificationRequest(driverId, rideId); // rideId puede ser null
            notificationServiceClient.notifyDriverRideTaken(request);
            log.info("Notificación de viaje tomado enviada a conductor {}.", driverId);
        } catch (Exception e) {
            log.error("Fallo al enviar notificación de viaje tomado a conductor {}: {}", driverId, e.getMessage(), e);
        }
    }

    // --- Mapeador Entidad Ride -> RideDto (Incluyendo shortId) ---
    private RideDto mapRideToDto(Ride ride) {
        if (ride == null) return null;
        return new RideDto(
                ride.getId(),
                ride.getShortId(), // Mapear shortId
                ride.getUserId(),
                ride.getAssignedDriverId(),
                ride.getAssignedVehicleId(),
                ride.getServiceType(),
                ride.getRideStatus(),
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

    // --- Replicar Enum DriverStatus aquí o usar módulo Common ---
    // Necesario para updateDriverStatusAsync y validateDriverCanAccept
    // Si no quieres replicarlo, tendrías que recibir/enviar Strings de estado
    // y convertir/comparar con Strings. Usar el Enum es más seguro.
    private enum DriverStatus {
        PENDING_APPROVAL, ACTIVE_OFFLINE, ACTIVE_AVAILABLE, ON_RIDE, SUSPENDED, REJECTED
    }

}