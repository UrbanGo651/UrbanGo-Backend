package com.urbango.ridesservice.controller;

import com.urbango.ridesservice.dto.CreateRideRequestDto;
import com.urbango.ridesservice.dto.RideDto;
import com.urbango.ridesservice.service.RideService;
import jakarta.persistence.EntityNotFoundException; // Importar para manejo de errores
import jakarta.validation.Valid; // Para validar el DTO de entrada
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException; // Para devolver errores HTTP

import java.util.Map; // Para cuerpos de solicitud flexibles
import java.util.Optional;
import java.util.UUID;

@RestController // Indica que es un controlador REST
@RequestMapping("/api/v1/rides") // Ruta base para todas las operaciones de viajes
@RequiredArgsConstructor // Inyección de dependencias vía constructor Lombok
@Slf4j // Logger de Lombok
public class RideController {

    private final RideService rideService; // Inyecta la interfaz del servicio

    /**
     * Endpoint para que un usuario solicite un nuevo viaje.
     * @param requestDto DTO con userId y serviceType.
     * @return ResponseEntity con el RideDto creado y estado 201, o error.
     */
    @PostMapping
    public ResponseEntity<RideDto> requestRide(@Valid @RequestBody CreateRideRequestDto requestDto) {
        log.info("POST /api/v1/rides - Solicitud recibida: {}", requestDto);
        try {
            RideDto createdRide = rideService.requestRide(requestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRide);
        } catch (EntityNotFoundException e) { // Captura si el usuario no existe (lanzado desde el service)
            log.warn("Error al solicitar viaje - Usuario no encontrado: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (IllegalStateException e) { // Captura si el usuario ya tiene un viaje activo
            log.warn("Error al solicitar viaje - Conflicto: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage(), e);
        } catch (Exception e) { // Captura otros errores inesperados
            log.error("Error inesperado al solicitar viaje", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al procesar la solicitud de viaje.", e);
        }
    }

    /**
     * Endpoint para obtener los detalles de un viaje específico por su ID.
     * @param rideId El UUID del viaje.
     * @return ResponseEntity con el RideDto y estado 200 si se encuentra, o 404 si no.
     */
    @GetMapping("/{rideId}")
    public ResponseEntity<RideDto> getRideById(@PathVariable UUID rideId) {
        log.info("GET /api/v1/rides/{} - Solicitud recibida", rideId);
        Optional<RideDto> rideDtoOptional = rideService.getRideById(rideId);
        return rideDtoOptional
                .map(ResponseEntity::ok) // Si existe, devuelve 200 OK con el DTO
                .orElse(ResponseEntity.notFound().build()); // Si no existe, devuelve 404 Not Found
    }

    /**
     * Endpoint para que un conductor acepte un viaje pendiente.
     * Se espera un cuerpo JSON con "driverId" y "vehicleId".
     * @param rideId El UUID del viaje a aceptar.
     * @param acceptRequest Map que contiene driverId y vehicleId.
     * @return ResponseEntity con el RideDto actualizado (estado ASSIGNED) y 200 OK, o error.
     */
    @PostMapping("/{rideId}/accept")
    public ResponseEntity<RideDto> acceptRide(@PathVariable UUID rideId, @RequestBody Map<String, String> acceptRequest) {
        UUID driverId;
        UUID vehicleId;
        try {
            // Extraer y validar los UUIDs del cuerpo de la solicitud
            driverId = UUID.fromString(acceptRequest.get("driverId"));
            vehicleId = UUID.fromString(acceptRequest.get("vehicleId"));
        } catch (Exception e) {
            log.warn("POST /api/v1/rides/{}/accept - Solicitud con IDs inválidos: {}", rideId, acceptRequest, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El cuerpo JSON debe contener 'driverId' y 'vehicleId' válidos como UUID.", e);
        }

        log.info("POST /api/v1/rides/{}/accept - Solicitud recibida de conductor {} con vehículo {}", rideId, driverId, vehicleId);
        try {
            RideDto acceptedRide = rideService.acceptRide(rideId, driverId, vehicleId);
            return ResponseEntity.ok(acceptedRide);
        } catch (EntityNotFoundException e) { // Viaje no encontrado
            log.warn("Error al aceptar viaje {} - No encontrado: {}", rideId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (IllegalStateException | SecurityException e) { // Viaje no disponible o error de seguridad
            log.warn("Error al aceptar viaje {} - Conflicto/Prohibido: {}", rideId, e.getMessage());
            // 409 Conflict es apropiado si el viaje ya fue tomado o está en un estado incorrecto
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado al aceptar viaje {}", rideId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al procesar la aceptación del viaje.", e);
        }
    }

    /**
     * Endpoint para que un conductor marque un viaje como completado.
     * Se espera un cuerpo JSON con "driverId" para validación.
     * @param rideId El UUID del viaje a completar.
     * @param completeRequest Map que contiene driverId.
     * @return ResponseEntity con el RideDto actualizado (estado COMPLETED) y 200 OK, o error.
     */
    @PostMapping("/{rideId}/complete")
    public ResponseEntity<RideDto> completeRide(@PathVariable UUID rideId, @RequestBody Map<String, String> completeRequest) {
        UUID driverId;
        try {
            // Extraer y validar el driverId del cuerpo
            driverId = UUID.fromString(completeRequest.get("driverId"));
        } catch (Exception e) {
            log.warn("POST /api/v1/rides/{}/complete - Solicitud con driverId inválido: {}", rideId, completeRequest, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El cuerpo JSON debe contener 'driverId' válido como UUID.", e);
        }

        log.info("POST /api/v1/rides/{}/complete - Solicitud recibida de conductor {}", rideId, driverId);
        try {
            RideDto completedRide = rideService.completeRide(rideId, driverId);
            return ResponseEntity.ok(completedRide);
        } catch (EntityNotFoundException e) { // Viaje no encontrado
            log.warn("Error al completar viaje {} - No encontrado: {}", rideId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (IllegalStateException | SecurityException e) { // Estado inválido o no es el conductor asignado
            log.warn("Error al completar viaje {} - Prohibido/Conflicto: {}", rideId, e.getMessage());
            // 403 Forbidden indica que la acción no está permitida para este usuario/estado
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado al completar viaje {}", rideId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al procesar la finalización del viaje.", e);
        }
    }

    /**
     * Endpoint para cancelar un viaje.
     * Se puede enviar opcionalmente una razón en el cuerpo: { "reason": "..." }
     * @param rideId El UUID del viaje a cancelar.
     * @param cancelRequest Map opcional con la razón.
     * @return ResponseEntity con el RideDto actualizado (estado cancelado) y 200 OK, o error.
     */
    @PostMapping("/{rideId}/cancel")
    public ResponseEntity<RideDto> cancelRide(@PathVariable UUID rideId, @RequestBody(required = false) Map<String, String> cancelRequest) {
        // Extrae la razón o usa un valor por defecto si el cuerpo es nulo o no tiene 'reason'
        String reason = (cancelRequest != null) ? cancelRequest.getOrDefault("reason", "CANCELLED_API") : "CANCELLED_API";

        log.info("POST /api/v1/rides/{}/cancel - Solicitud recibida con razón: {}", rideId, reason);
        try {
            RideDto cancelledRide = rideService.cancelRide(rideId, reason);
            return ResponseEntity.ok(cancelledRide);
        } catch (EntityNotFoundException e) { // Viaje no encontrado
            log.warn("Error al cancelar viaje {} - No encontrado: {}", rideId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (IllegalStateException e) { // Viaje ya finalizado o cancelado
            log.warn("Error al cancelar viaje {} - Conflicto: {}", rideId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado al cancelar viaje {}", rideId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al procesar la cancelación del viaje.", e);
        }
    }

    /**
     * Endpoint para buscar el viaje activo (REQUESTED o ASSIGNED) de un usuario.
     * @param userId El UUID del usuario.
     * @return ResponseEntity con el RideDto y 200 OK si existe, o 204 No Content si no.
     */
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<RideDto> getActiveRideForUser(@PathVariable UUID userId) {
        log.info("GET /api/v1/rides/user/{}/active - Solicitud recibida", userId);
        Optional<RideDto> rideDtoOptional = rideService.findActiveRideForUser(userId);
        return rideDtoOptional
                .map(ResponseEntity::ok) // 200 OK
                .orElse(ResponseEntity.noContent().build()); // 204 No Content
    }

    /**
     * Endpoint para buscar el viaje activo (ASSIGNED) de un conductor.
     * @param driverId El UUID del conductor.
     * @return ResponseEntity con el RideDto y 200 OK si existe, o 204 No Content si no.
     */
    @GetMapping("/driver/{driverId}/active")
    public ResponseEntity<RideDto> getActiveRideForDriver(@PathVariable UUID driverId) {
        log.info("GET /api/v1/rides/driver/{}/active - Solicitud recibida", driverId);
        Optional<RideDto> rideDtoOptional = rideService.findActiveRideForDriver(driverId);
        return rideDtoOptional
                .map(ResponseEntity::ok) // 200 OK
                .orElse(ResponseEntity.noContent().build()); // 204 No Content
    }

    /**
     * Endpoint para buscar un viaje por su ID corto (generado internamente).
     * @param shortId El ID corto de 8 caracteres hexadecimales.
     * @return ResponseEntity con el RideDto y 200 OK si se encuentra, o 404 si no.
     */
    @GetMapping("/short/{shortId}") // <<< NUEVO ENDPOINT
    public ResponseEntity<RideDto> getRideByShortId(@PathVariable String shortId) {
        // Validar formato básico del shortId
        if (shortId == null || !shortId.matches("^[a-fA-F0-9]{8}$")) {
            log.warn("GET /api/v1/rides/short/{} - Solicitud recibida con formato de ID corto inválido", shortId);
            return ResponseEntity.badRequest().build(); // 400 Bad Request
        }

        log.info("GET /api/v1/rides/short/{} - Solicitud recibida", shortId);
        // Llamar al método del servicio que busca por shortId
        return rideService.findRideByShortId(shortId)
                .map(ResponseEntity::ok) // 200 OK si se encuentra
                .orElse(ResponseEntity.notFound().build()); // 404 Not Found si no
    }

}
