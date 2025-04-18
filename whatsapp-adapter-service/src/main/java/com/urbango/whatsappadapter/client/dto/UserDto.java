package com.urbango.whatsappadapter.client.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.UUID;
// No necesitamos Instant aquí si no lo usamos

/**
 * Representa la información mínima esperada de un User desde UserServiceClient.
 * Utilizado para deserializar la respuesta del Feign Client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    // Necesario para identificar al usuario internamente
    private UUID id;

    // Necesario para identificar y potencialmente responder (aunque la respuesta la maneja WhatsAppResponseService)
    private String whatsappNumber;

    // Útil para mensajes personalizados si fuera necesario
    private String fullName;

    // El 'status' del usuario probablemente no es relevante para el adaptador
    // private String status;
    // private java.time.Instant createdAt;
    // private java.time.Instant updatedAt;
}
