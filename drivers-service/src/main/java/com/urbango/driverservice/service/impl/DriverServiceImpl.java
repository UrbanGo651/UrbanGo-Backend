package com.urbango.driverservice.service.impl;

import com.urbango.driverservice.dto.*; // Importar todos los DTOs
import com.urbango.driverservice.entity.Driver;
import com.urbango.driverservice.entity.DriverDocument;
import com.urbango.driverservice.entity.Vehicle;
// Importar los Enums específicos
import com.urbango.driverservice.enums.DriverStatus;
import com.urbango.driverservice.enums.VehicleType;
import com.urbango.driverservice.enums.VerificationStatus; // Asegúrate de importar si lo usas aquí
import com.urbango.driverservice.enums.DocumentType;     // Asegúrate de importar si lo usas aquí
// Importar Repositorios
import com.urbango.driverservice.repository.DriverDocumentRepository;
import com.urbango.driverservice.repository.DriverRepository;
import com.urbango.driverservice.repository.VehicleRepository;
// Importar excepciones y otras clases necesarias
import com.urbango.driverservice.service.DriverService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
// Importar utilidades de Java
import java.time.Instant;
import java.util.ArrayList; // Importar ArrayList explícitamente
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class DriverServiceImpl implements DriverService {

    // Inyección de dependencias vía constructor (gracias a @RequiredArgsConstructor)
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverDocumentRepository documentRepository;

    @Override
    @Transactional // Asegura que la operación se ejecute dentro de una transacción
    public DriverDto registerDriver(RegisterDriverRequestDto requestDto) {
        log.info("Intentando registrar conductor con WhatsApp: {}", requestDto.getWhatsappNumber());

        // La validación del formato del número de WhatsApp se hace en el DTO con @Pattern
        // Considerar normalización (quitar espacios) aquí si se desea mayor robustez
        String whatsappNumber = requestDto.getWhatsappNumber(); // .replaceAll("\\s+", ""); // <- Normalización opcional

        // Verificar si el número de WhatsApp ya existe
        driverRepository.findByWhatsappNumber(whatsappNumber).ifPresent(existingDriver -> {
            log.warn("Intento de registrar conductor con WhatsApp existente: {}", whatsappNumber);
            throw new IllegalArgumentException("El número de WhatsApp '" + whatsappNumber + "' ya está registrado como conductor.");
        });

        // Mapear DTO a Entidad
        Driver newDriver = new Driver();
        newDriver.setWhatsappNumber(whatsappNumber);
        newDriver.setFullName(requestDto.getFullName());
        newDriver.setDriverStatus(DriverStatus.PENDING_APPROVAL); // Usar Enum para estado inicial

        // Guardar en la BD
        Driver savedDriver = driverRepository.save(newDriver);
        log.info("Conductor registrado (pendiente aprobación) con ID: {}", savedDriver.getId());

        // Mapear Entidad guardada a DTO de respuesta (simple, sin detalles de vehículos/docs)
        return mapDriverToSimpleDto(savedDriver);
    }

    @Override
    @Transactional(readOnly = true) // Transacción de solo lectura, optimiza rendimiento
    public Optional<DriverDto> findDriverByWhatsappNumber(String whatsappNumber) {
        log.debug("Buscando conductor por WhatsApp: {}", whatsappNumber);
        // Considerar normalización aquí también si se implementó en registerDriver
        return driverRepository.findByWhatsappNumber(whatsappNumber)
                .map(this::mapDriverToSimpleDto); // Mapea a DTO simple
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DriverDto> findDriverById(UUID id) {
        log.debug("Buscando conductor por ID: {}", id);
        return driverRepository.findById(id)
                .map(this::mapDriverToSimpleDto); // Mapea a DTO simple
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DriverDto> getDriverDetails(UUID id) {
        log.debug("Obteniendo detalles completos del conductor con ID: {}", id);
        // findById cargará las colecciones LAZY necesarias dentro de la transacción
        return driverRepository.findById(id)
                .map(this::mapDriverToFullDto); // Mapea a DTO completo (con vehículos/documentos)
    }

    @Override
    @Transactional
    public DriverDto updateDriverStatus(UUID driverId, String newStatusString) {
        log.info("Actualizando estado del conductor {} a {}", driverId, newStatusString);

        DriverStatus newStatus;
        try {
            // Convertir el String de entrada (posiblemente desde la API) al Enum correspondiente.
            // toUpperCase() ayuda a que coincida sin importar mayúsculas/minúsculas.
            newStatus = DriverStatus.valueOf(newStatusString.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Si valueOf falla, significa que el string no corresponde a ninguna constante Enum.
            log.error("Intento de actualizar a estado inválido: {}", newStatusString);
            throw new IllegalArgumentException("Estado '" + newStatusString + "' no es válido.");
        }

        // Buscar el conductor o lanzar excepción si no existe
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new EntityNotFoundException("Conductor no encontrado con ID: " + driverId));

        // Aquí se podrían añadir reglas de negocio para transiciones de estado válidas si fuera necesario
        // (ej: no se puede pasar de SUSPENDED a AVAILABLE sin una acción específica).

        log.debug("Cambiando estado de {} de {} a {}", driverId, driver.getDriverStatus(), newStatus);
        driver.setDriverStatus(newStatus); // Establecer el nuevo estado Enum
        Driver updatedDriver = driverRepository.save(driver);

        // Devolver el DTO simple actualizado
        return mapDriverToSimpleDto(updatedDriver);
    }

    @Override
    @Transactional
    public DriverDto approveDriver(UUID driverId, UUID adminId) {
        log.info("Aprobando conductor {} por administrador {}", driverId, adminId);
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new EntityNotFoundException("Conductor no encontrado con ID: " + driverId));

        // Verificar que el conductor esté en el estado correcto para ser aprobado
        if (driver.getDriverStatus() != DriverStatus.PENDING_APPROVAL) { // Comparación directa de Enums
            log.warn("Intento de aprobar conductor {} que no está en estado PENDING (estado actual: {})", driverId, driver.getDriverStatus());
            throw new IllegalStateException("Solo se pueden aprobar conductores en estado PENDING_APPROVAL.");
        }

        // Actualizar estado y campos de auditoría
        driver.setDriverStatus(DriverStatus.ACTIVE_OFFLINE); // Usar Enum
        driver.setApprovedBy(adminId); // Guardar quién aprobó
        driver.setApprovalTimestamp(Instant.now()); // Guardar cuándo se aprobó

        Driver approvedDriver = driverRepository.save(driver);
        log.info("Conductor {} aprobado exitosamente.", driverId);

        // Devolver DTO simple
        return mapDriverToSimpleDto(approvedDriver);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DriverDto> findAvailableDrivers(String vehicleTypeString) {
        log.debug("Buscando conductores disponibles para tipo de vehículo: {}", vehicleTypeString);

        VehicleType vehicleType;
        try {
            // Convertir el String (de la API/solicitud) al Enum VehicleType
            vehicleType = VehicleType.valueOf(vehicleTypeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Tipo de vehículo inválido solicitado: {}", vehicleTypeString);
            // Si el tipo de vehículo no es válido, no podemos encontrar conductores.
            return List.of(); // Devolver lista vacía
        }

        // 1. Buscar conductores con estado ACTIVE_AVAILABLE usando el Enum
        List<Driver> availableDriversBase = driverRepository.findByDriverStatus(DriverStatus.ACTIVE_AVAILABLE);

        // 2. Filtrar en memoria los que tienen un vehículo activo del tipo Enum especificado
        List<Driver> filteredDrivers = availableDriversBase.stream()
                .filter(driver -> hasActiveVehicleOfType(driver, vehicleType)) // Pasar el Enum
                .toList(); // Java 16+

        log.info("Encontrados {} conductores disponibles para {}", filteredDrivers.size(), vehicleType);

        // 3. Mapear los resultados a DTOs (simples)
        return filteredDrivers.stream()
                .map(this::mapDriverToSimpleDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VehicleDto addVehicleToDriver(UUID driverId, CreateVehicleRequestDto vehicleDto) {
        log.info("Intentando añadir vehículo con placa {} al conductor {}", vehicleDto.getLicensePlate(), driverId);

        // 1. Buscar al conductor
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new EntityNotFoundException("Conductor no encontrado con ID: " + driverId));

        // 2. Validar si la placa ya existe en el sistema (las placas deben ser únicas)
        String licensePlateUpper = vehicleDto.getLicensePlate().toUpperCase(); // Guardar en mayúsculas por consistencia
        vehicleRepository.findByLicensePlate(licensePlateUpper).ifPresent(existingVehicle -> {
            log.warn("Intento de añadir vehículo con placa existente: {}", licensePlateUpper);
            throw new IllegalArgumentException("La placa '" + licensePlateUpper + "' ya está registrada.");
        });

        // 3.Crear la nueva entidad Vehículo
        Vehicle newVehicle = new Vehicle();
        newVehicle.setLicensePlate(licensePlateUpper);
        newVehicle.setVehicleType(vehicleDto.getVehicleType()); // Enum
        newVehicle.setModel(vehicleDto.getModel());
        newVehicle.setColor(vehicleDto.getColor());
        newVehicle.setSoatExpiryDate(vehicleDto.getSoatExpiryDate());
        newVehicle.setTechnoExpiryDate(vehicleDto.getTechnoExpiryDate());
        newVehicle.setActive(true); // Por defecto, un vehículo nuevo está activo

        // 4. *** Asociar el vehículo con el conductor ***
        newVehicle.setDriver(driver);
        // Opcionalmente, si tienes el metodo helper en Driver: driver.addVehicle(newVehicle);

        // 5. Guardar el nuevo vehículo
        Vehicle savedVehicle = vehicleRepository.save(newVehicle);
        log.info("Vehículo con ID {} añadido exitosamente al conductor {}", savedVehicle.getId(), driverId);

        // 6. Mapear la entidad guardada a DTO y devolver
        return mapVehicleToDto(savedVehicle); // Usa el método auxiliar que ya teníamos
    }

    // --- Métodos Auxiliares Internos ---

    /**
     * Verifica si un conductor tiene al menos un vehículo activo del tipo especificado.
     * Asegura que la colección de vehículos se cargue si es LAZY (lo cual @Transactional debería hacer).
     * @param driver El conductor a verificar.
     * @param vehicleType El tipo de vehículo Enum a buscar.
     * @return true si tiene al menos un vehículo activo de ese tipo, false en caso contrario.
     */
    private boolean hasActiveVehicleOfType(Driver driver, VehicleType vehicleType) {
        if (driver.getVehicles() == null) {
            log.warn("La colección de vehículos es nula para el conductor {}, no se puede verificar tipo.", driver.getId());
            return false; // Seguridad: si la colección es nula, no tiene vehículos.
        }
        return driver.getVehicles().stream()
                .anyMatch(vehicle -> vehicle.isActive() && vehicle.getVehicleType() == vehicleType); // Comparación directa de Enums
    }


    // --- Métodos de Mapeo Entidad -> DTO ---

    /**
     * Mapea una entidad Driver a un DriverDto simple (sin listas detalladas).
     */
    private DriverDto mapDriverToSimpleDto(Driver driver) {
        if (driver == null) {
            return null;
        }
        // Creamos listas vacías para el DTO simple
        return new DriverDto(
                driver.getId(),
                driver.getWhatsappNumber(),
                driver.getFullName(),
                driver.getDriverStatus(), // Mapea el Enum directamente
                driver.getApprovalTimestamp(),
                driver.getCreatedAt(),
                driver.getUpdatedAt(),
                new ArrayList<>(), // Lista vacía de vehículos
                new ArrayList<>()  // Lista vacía de documentos
        );
    }

    /**
     * Mapea una entidad Driver a un DriverDto completo, incluyendo listas de vehículos y documentos.
     */
    private DriverDto mapDriverToFullDto(Driver driver) {
        if (driver == null) {
            return null;
        }

        // Mapear la lista de vehículos (si existe) a lista de VehicleDto
        List<VehicleDto> vehicleDtos = (driver.getVehicles() == null) ? new ArrayList<>() :
                driver.getVehicles().stream()
                        .map(this::mapVehicleToDto) // Llama al mapeador de vehículos
                        .collect(Collectors.toList());

        // Mapear la lista de documentos (si existe) a lista de DriverDocumentDto
        List<DriverDocumentDto> documentDtos = (driver.getDocuments() == null) ? new ArrayList<>() :
                driver.getDocuments().stream()
                        .map(this::mapDocumentToDto) // Llama al mapeador de documentos
                        .collect(Collectors.toList());

        return new DriverDto(
                driver.getId(),
                driver.getWhatsappNumber(),
                driver.getFullName(),
                driver.getDriverStatus(), // Mapea el Enum directamente
                driver.getApprovalTimestamp(),
                driver.getCreatedAt(),
                driver.getUpdatedAt(),
                vehicleDtos,      // Lista mapeada de vehículos
                documentDtos      // Lista mapeada de documentos
        );
    }

    /**
     * Mapea una entidad Vehicle a un VehicleDto.
     */
    private VehicleDto mapVehicleToDto(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }
        return new VehicleDto(
                vehicle.getId(),
                vehicle.getLicensePlate(),
                vehicle.getVehicleType(), // Mapea el Enum directamente
                vehicle.getModel(),
                vehicle.getColor(),
                vehicle.getSoatExpiryDate(),
                vehicle.getTechnoExpiryDate(),
                vehicle.isActive(),
                vehicle.getCreatedAt(),
                vehicle.getUpdatedAt()
        );
    }

    /**
     * Mapea una entidad DriverDocument a un DriverDocumentDto.
     */
    private DriverDocumentDto mapDocumentToDto(DriverDocument document) {
        if (document == null) {
            return null;
        }
        return new DriverDocumentDto(
                document.getId(),
                document.getDocumentType(), // Mapea el Enum directamente
                document.getVerificationStatus(), // Mapea el Enum directamente
                document.getVerifierNotes(),
                document.getIssueDate(),
                document.getExpiryDate(),
                document.getVerifiedAt(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}
