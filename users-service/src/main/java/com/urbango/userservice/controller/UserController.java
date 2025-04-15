package com.urbango.userservice.controller;

import com.urbango.userservice.dto.CreateUserRequestDto;
import com.urbango.userservice.dto.UserDto;
import com.urbango.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@RestController // Combina @Controller y @ResponseBody, indica que es un controlador REST
@RequestMapping("/api/v1/users") // Define la ruta base para todos los endpoints de este controlador
@RequiredArgsConstructor // Lombok: para inyección de dependencias por constructor
@Slf4j // Lombok: para logging
public class UserController {

    private final UserService userService; // Inyección del servicio

    /**
     * Endpoint para crear un nuevo usuario.
     * POST /api/v1/users
     * Body: { "whatsappNumber": "...", "fullName": "..." }
     */
    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequestDto requestDto) {
        log.info("Recibida solicitud POST para crear usuario: {}", requestDto.getWhatsappNumber());
        try {
            UserDto createdUser = userService.createUser(requestDto);
            // Devolvemos 201 Created y el usuario creado en el cuerpo
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (IllegalArgumentException e) { // Captura la excepción si el WhatsApp ya existe (del service)
            log.warn("Error al crear usuario: {}", e.getMessage());
            // Devolvemos 409 Conflict (o 400 Bad Request si prefieres)
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage(), e);
        } catch (Exception e) { // Captura genérica para otros posibles errores
            log.error("Error inesperado al crear usuario", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al procesar la solicitud", e);
        }
    }

    /**
     * Endpoint para obtener un usuario por su número de WhatsApp.
     * GET /api/v1/users/whatsapp/{whatsappNumber}
     */
    @GetMapping("/whatsapp/{whatsappNumber}")
    public ResponseEntity<UserDto> getUserByWhatsappNumber(@PathVariable String whatsappNumber) {
        log.info("Recibida solicitud GET para usuario con WhatsApp: {}", whatsappNumber);
        Optional<UserDto> userDtoOptional = userService.findUserByWhatsappNumber(whatsappNumber);

        // Forma 1: Usando ifPresentOrElse (Java 9+)
        // return userDtoOptional
        //        .map(ResponseEntity::ok) // Si existe, devuelve 200 OK con el DTO
        //        .orElse(ResponseEntity.notFound().build()); // Si no existe, devuelve 404 Not Found

        // Forma 2: Usando if tradicional (más compatible)
        if (userDtoOptional.isPresent()) {
            return ResponseEntity.ok(userDtoOptional.get()); // 200 OK con el DTO
        } else {
            log.warn("Usuario no encontrado con WhatsApp: {}", whatsappNumber);
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }

    /**
     * Endpoint para obtener un usuario por su ID.
     * GET /api/v1/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable UUID id) {
        log.info("Recibida solicitud GET para usuario con ID: {}", id);
        Optional<UserDto> userDtoOptional = userService.findUserById(id);

        // Similar al anterior, podemos usar map/orElse o if/else
        return userDtoOptional
                .map(user -> ResponseEntity.ok(user)) // Equivalente a .map(ResponseEntity::ok)
                .orElseGet(() -> { // orElseGet es ligeramente más eficiente que orElse si la creación del valor alternativo es costosa
                    log.warn("Usuario no encontrado con ID: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    // --- Manejo de Excepciones Centralizado (Alternativa) ---
    // Podría crear una clase anotada con @ControllerAdvice para manejar
    // excepciones específicas (como la de WhatsApp duplicado) de forma
    // centralizada y devolver respuestas de error consistentes.
    // Por ahora, el manejo dentro de createUser es suficiente para empezar.
}
