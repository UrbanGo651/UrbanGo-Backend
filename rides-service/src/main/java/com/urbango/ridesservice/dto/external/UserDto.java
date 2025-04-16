package com.urbango.ridesservice.dto.external;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Representa la estructura de datos de un usuario como la devuelve user-service.
 * Usado por el Feign Client para deserializar la respuesta.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private UUID id;
    private String whatsappNumber;
    private String fullName;
    private String status; // Mantenido como String por simplicidad en la comunicación
    private Instant createdAt;
    private Instant updatedAt;
}
