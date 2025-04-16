package com.urbango.driverservice.controller;

import com.urbango.driverservice.dto.CreateVehicleRequestDto;
import com.urbango.driverservice.dto.DriverDto;
import com.urbango.driverservice.dto.RegisterDriverRequestDto;
import com.urbango.driverservice.dto.VehicleDto;
import com.urbango.driverservice.service.DriverService;
import jakarta.persistence.EntityNotFoundException; // Importar excepción
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map; // Para recibir el estado en el PATCH
import java.util.Optional;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/drivers") // Ruta base para conductores
@RequiredArgsConstructor
@Slf4j
public class DriverController {

    private final DriverService driverService;

    /**
     * Endpoint para registrar un nuevo conductor (inicial).
     * POST /api/v1/drivers
     * Body: { "whatsappNumber": "+57...", "fullName": "..." }
     */
    @PostMapping
    public ResponseEntity<DriverDto> registerDriver(@Valid @RequestBody RegisterDriverRequestDto requestDto) {
        log.info("Recibida solicitud POST para registrar conductor: {}", requestDto.getWhatsappNumber());
        try {
            DriverDto registeredDriver = driverService.registerDriver(requestDto);
            // 201 Created con el DTO simple del conductor
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredDriver);
        } catch (IllegalArgumentException e) { // Captura si WhatsApp ya existe
            log.warn("Error al registrar conductor: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado al registrar conductor", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al procesar la solicitud", e);
        }
    }

    /**
     * Endpoint para obtener un conductor por su número de WhatsApp.
     * GET /api/v1/drivers/whatsapp/{whatsappNumber}
     */
    @GetMapping("/whatsapp/{whatsappNumber}")
    public ResponseEntity<DriverDto> getDriverByWhatsappNumber(@PathVariable String whatsappNumber) {
        log.info("Recibida solicitud GET para conductor con WhatsApp: {}", whatsappNumber);
        Optional<DriverDto> driverDtoOptional = driverService.findDriverByWhatsappNumber(whatsappNumber);
        return driverDtoOptional
                .map(ResponseEntity::ok) // 200 OK con DTO simple
                .orElse(ResponseEntity.notFound().build()); // 404 Not Found
    }

    /**
     * Endpoint para obtener un conductor por su ID (información básica).
     * GET /api/v1/drivers/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<DriverDto> getDriverById(@PathVariable UUID id) {
        log.info("Recibida solicitud GET para conductor con ID (simple): {}", id);
        Optional<DriverDto> driverDtoOptional = driverService.findDriverById(id);
        return driverDtoOptional
                .map(ResponseEntity::ok) // 200 OK con DTO simple
                .orElse(ResponseEntity.notFound().build()); // 404 Not Found
    }

    /**
     * Endpoint para obtener los detalles completos de un conductor (con vehículos/documentos).
     * GET /api/v1/drivers/{id}/details
     */
    @GetMapping("/{id}/details")
    public ResponseEntity<DriverDto> getDriverDetailsById(@PathVariable UUID id) {
        log.info("Recibida solicitud GET para conductor con ID (detalles): {}", id);
        Optional<DriverDto> driverDtoOptional = driverService.getDriverDetails(id);
        return driverDtoOptional
                .map(ResponseEntity::ok) // 200 OK con DTO completo
                .orElse(ResponseEntity.notFound().build()); // 404 Not Found
    }

    /**
     * Endpoint para actualizar el estado de un conductor.
     * PATCH /api/v1/drivers/{id}/status
     * Body: { "status": "ACTIVE_AVAILABLE" } // o cualquier otro estado válido
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<DriverDto> updateDriverStatus(@PathVariable UUID id, @RequestBody Map<String, String> statusUpdate) {
        String newStatus = statusUpdate.get("status");
        if (newStatus == null || newStatus.isBlank()) {
            log.warn("Solicitud PATCH para estado de conductor {} sin valor de 'status'", id);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud debe contener la clave 'status' con el nuevo estado.");
        }
        log.info("Recibida solicitud PATCH para actualizar estado de conductor {} a {}", id, newStatus);
        try {
            DriverDto updatedDriver = driverService.updateDriverStatus(id, newStatus);
            return ResponseEntity.ok(updatedDriver); // 200 OK con el DTO simple actualizado
        } catch (EntityNotFoundException e) {
            log.warn("Error al actualizar estado: Conductor no encontrado - {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (IllegalArgumentException e) { // Captura si el estado no es válido
            log.warn("Error al actualizar estado: Estado inválido - {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado al actualizar estado del conductor {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al procesar la solicitud", e);
        }
    }

    /**
     * Endpoint para simular la aprobación de un conductor (cambia estado a ACTIVE_OFFLINE).
     * POST /api/v1/drivers/{id}/approve
     * Body: (Opcional) { "adminId": "uuid-del-admin" } // Si queremos registrar quién aprueba
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<DriverDto> approveDriver(@PathVariable UUID id, @RequestBody(required = false) Map<String, String> approvalInfo) {
        // Obtener adminId del cuerpo si se envía, si no, usar un UUID genérico o null
        UUID adminId = null;
        if (approvalInfo != null && approvalInfo.containsKey("adminId")) {
            try {
                adminId = UUID.fromString(approvalInfo.get("adminId"));
            } catch (IllegalArgumentException e) {
                log.warn("ID de admin inválido proporcionado para aprobar conductor {}", id);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El 'adminId' proporcionado no es un UUID válido.");
            }
        } else {
            // Podríamos generar un UUID de sistema o dejarlo null si no es estrictamente necesario ahora
            log.warn("No se proporcionó adminId para la aprobación del conductor {}. Usando null.", id);
        }

        log.info("Recibida solicitud POST para aprobar conductor {} (Admin: {})", id, adminId);
        try {
            DriverDto approvedDriver = driverService.approveDriver(id, adminId);
            return ResponseEntity.ok(approvedDriver); // 200 OK con DTO simple
        } catch (EntityNotFoundException e) {
            log.warn("Error al aprobar: Conductor no encontrado - {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (IllegalStateException e) { // Captura si no está en estado PENDING
            log.warn("Error al aprobar conductor: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage(), e); // 409 Conflict o 400 Bad Request
        } catch (Exception e) {
            log.error("Error inesperado al aprobar conductor {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al procesar la solicitud", e);
        }
    }

    /**
     * Endpoint para buscar conductores disponibles por tipo de vehículo.
     * GET /api/v1/drivers/available?vehicleType=MOTORCYCLE
     */
    @GetMapping("/available")
    public ResponseEntity<List<DriverDto>> findAvailableDrivers(
            @RequestParam(required = true) String vehicleType) { // Recibe String
        log.info("Recibida solicitud GET para conductores disponibles con tipo de vehículo: {}", vehicleType);
        try {
            List<DriverDto> availableDrivers = driverService.findAvailableDrivers(vehicleType);
            return ResponseEntity.ok(availableDrivers); // 200 OK con la lista (puede estar vacía)
        } catch (Exception e) {
            // Aunque el servicio ya maneja tipos inválidos devolviendo lista vacía,
            // podríamos añadir un manejo de error aquí si fuera necesario.
            log.error("Error inesperado al buscar conductores disponibles para {}", vehicleType, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al procesar la solicitud", e);
        }
    }

    /**
     * Endpoint para añadir un vehículo a un conductor existente.
     * POST /api/v1/drivers/{driverId}/vehicles
     * Body: { "licensePlate": "...", "vehicleType": "MOTORCYCLE", ... }
     */
    @PostMapping("/{driverId}/vehicles")
    public ResponseEntity<VehicleDto> addVehicle(
            @PathVariable UUID driverId,
            @Valid @RequestBody CreateVehicleRequestDto requestDto) {

        log.info("POST /api/v1/drivers/{}/vehicles - Solicitud recibida: {}", driverId, requestDto);
        try {
            VehicleDto createdVehicle = driverService.addVehicleToDriver(driverId, requestDto);
            // Devolvemos 201 Created y el vehículo creado
            return ResponseEntity.status(HttpStatus.CREATED).body(createdVehicle);
        } catch (EntityNotFoundException e) { // Conductor no encontrado
            log.warn("Error al añadir vehículo - Conductor no encontrado: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (IllegalArgumentException e) { // Placa duplicada
            log.warn("Error al añadir vehículo - Conflicto: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado al añadir vehículo al conductor {}", driverId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al procesar la solicitud.", e);
        }
    }


    // --- Endpoints Futuros (para añadir vehículos, documentos) ---
    // @PostMapping("/{id}/vehicles") ...
    // @PostMapping("/{id}/documents") ...
}
