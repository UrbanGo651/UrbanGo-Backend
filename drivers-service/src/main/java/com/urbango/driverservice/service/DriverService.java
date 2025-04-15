package com.urbango.driverservice.service;

import com.urbango.driverservice.dto.DriverDto;
import com.urbango.driverservice.dto.RegisterDriverRequestDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DriverService {

    /**
     * Realiza el registro inicial de un conductor.
     * Establece el estado inicial como PENDING_APPROVAL.
     * @param requestDto DTO con la información inicial del conductor.
     * @return El DTO del conductor recién registrado (sin vehículos/documentos aún).
     * @throws IllegalArgumentException si el número de WhatsApp ya existe.
     */
    DriverDto registerDriver(RegisterDriverRequestDto requestDto);

    /**
     * Busca un conductor por su número de WhatsApp.
     * @param whatsappNumber El número de WhatsApp.
     * @return Un Optional conteniendo el DriverDto si se encuentra, o vacío si no.
     */
    Optional<DriverDto> findDriverByWhatsappNumber(String whatsappNumber);

    /**
     * Busca un conductor por su ID.
     * @param id El UUID del conductor.
     * @return Un Optional conteniendo el DriverDto si se encuentra, o vacío si no.
     */
    Optional<DriverDto> findDriverById(UUID id);

    /**
     * Obtiene los detalles completos de un conductor, incluyendo vehículos y documentos.
     * @param id El UUID del conductor.
     * @return Un Optional conteniendo el DriverDto completo si se encuentra, o vacío si no.
     */
    Optional<DriverDto> getDriverDetails(UUID id);

    /**
     * Cambia el estado de un conductor (ej: de PENDING_APPROVAL a ACTIVE_OFFLINE,
     * de ACTIVE_OFFLINE a ACTIVE_AVAILABLE, de ACTIVE_AVAILABLE a ON_RIDE, etc.).
     * @param driverId El ID del conductor.
     * @param newStatus El nuevo estado deseado (debe ser uno válido).
     * @return El DriverDto actualizado.
     * @throws jakarta.persistence.EntityNotFoundException si el conductor no existe.
     * @throws IllegalArgumentException si el cambio de estado no es válido.
     */
    DriverDto updateDriverStatus(UUID driverId, String newStatus);

    /**
     * Simula la aprobación de un conductor (parte del proceso manual inicial).
     * Cambia el estado de PENDING_APPROVAL a ACTIVE_OFFLINE.
     * @param driverId El ID del conductor a aprobar.
     * @param adminId El ID del administrador que aprueba (para auditoría futura).
     * @return El DriverDto actualizado.
     * @throws jakarta.persistence.EntityNotFoundException si el conductor no existe.
     * @throws IllegalStateException si el conductor no está en estado PENDING_APPROVAL.
     */
    DriverDto approveDriver(UUID driverId, UUID adminId);


    /**
     * Busca conductores que estén disponibles para recibir un servicio de un tipo específico.
     * Por ahora, busca por estado ACTIVE_AVAILABLE y que tengan al menos un vehículo activo del tipo dado.
     * (La lógica de proximidad geográfica se añadiría en fases posteriores).
     * @param vehicleType El tipo de vehículo requerido ("CAR", "MOTORCYCLE", etc.)
     * @return Una lista de DriverDto de los conductores disponibles (podría ser vacía).
     */
    List<DriverDto> findAvailableDrivers(String vehicleType);


    // --- Métodos Futuros (a añadir cuando se necesiten) ---
    /*
    VehicleDto addVehicleToDriver(UUID driverId, AddVehicleRequestDto vehicleDto);
    DriverDocumentDto addDocumentToDriver(UUID driverId, AddDocumentRequestDto documentDto);
    DriverDto updateDriverProfile(UUID driverId, UpdateDriverProfileDto profileDto);
    void deleteDriver(UUID driverId);
    */
}
