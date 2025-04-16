package com.urbango.notificationservice.dto.external;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.UUID;

/**
 * Representa la información mínima esperada de un User desde UserServiceClient.
 * Utilizado para deserializar la respuesta del Feign Client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    // ID es útil para confirmar que se obtuvo el usuario correcto
    private UUID id;

    // Campo ESENCIAL para enviar la notificación
    private String whatsappNumber;

    // Campo útil para personalizar mensajes
    private String fullName;

    // No necesitamos otros campos como status, createdAt, etc.,
    // a menos que un mensaje específico los requiera.
}
