package com.urbango.ridesservice.service;
import com.urbango.ridesservice.dto.CreateRideRequestDto;
import com.urbango.ridesservice.dto.RideDto;

import java.util.Optional;
import java.util.UUID;
public interface RideService {

    /**
     * Inicia una nueva solicitud de viaje.
     * Guarda el viaje en estado REQUESTED e inicia la búsqueda de conductores.
     * @param requestDto Datos de la solicitud (ID de usuario, tipo de servicio).
     * @return El DTO del viaje recién creado.
     * @throws // Excepciones por UserNotFound, UserHasActiveRide, etc. (Se añadirán)
     */
    RideDto requestRide(CreateRideRequestDto requestDto);

    /**
     * Procesa la aceptación de un viaje por parte de un conductor.
     * Cambia el estado del viaje a ASSIGNED y notifica al usuario y conductor.
     * @param rideId ID del viaje a aceptar.
     * @param driverId ID del conductor que acepta.
     * @param vehicleId ID del vehículo que usará el conductor.
     * @return El DTO del viaje actualizado.
     * @throws // Excepciones RideNotFound, RideAlreadyAssigned, DriverNotAvailable, etc.
     */
    RideDto acceptRide(UUID rideId, UUID driverId, UUID vehicleId); // Añadimos vehicleId

    /**
     * Marca un viaje como completado por el conductor.
     * Cambia el estado a COMPLETED y actualiza el estado del conductor.
     * @param rideId ID del viaje completado.
     * @param driverId ID del conductor que completa (para validación).
     * @return El DTO del viaje actualizado.
     * @throws // Excepciones RideNotFound, NotAssignedToThisDriver, etc.
     */
    RideDto completeRide(UUID rideId, UUID driverId);

    /**
     * Cancela un viaje (puede ser iniciado por usuario o sistema).
     * @param rideId ID del viaje a cancelar.
     * @param reason Razón de la cancelación (ej: "CANCELLED_USER", "TIMEOUT_NO_DRIVER").
     * @return El DTO del viaje actualizado.
     * @throws // Excepciones RideNotFound, CannotCancelCompletedRide, etc.
     */
    RideDto cancelRide(UUID rideId, String reason); // Razón puede ser un Enum en el futuro

    /**
     * Obtiene los detalles de un viaje por su ID.
     * @param rideId ID del viaje.
     * @return Optional con el RideDto si existe.
     */
    Optional<RideDto> getRideById(UUID rideId);

    /**
     * Busca si un usuario tiene un viaje activo (REQUESTED o ASSIGNED).
     * @param userId ID del usuario.
     * @return Optional con el RideDto del viaje activo si existe.
     */
    Optional<RideDto> findActiveRideForUser(UUID userId);

    /**
     * Busca si un conductor tiene un viaje asignado (ASSIGNED).
     * @param driverId ID del conductor.
     * @return Optional con el RideDto del viaje asignado si existe.
     */
    Optional<RideDto> findActiveRideForDriver(UUID driverId);
}
