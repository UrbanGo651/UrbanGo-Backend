package com.urbango.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequestDto {

    @NotBlank(message = "El número de WhatsApp no puede estar vacío")
    @Size(min = 5, max = 25, message = "El número de WhatsApp debe tener entre 5 y 25 caracteres") // Ajusta según necesidad
    private String whatsappNumber;

    @NotBlank(message = "El nombre completo no puede estar vacío")
    @Size(max = 255, message = "El nombre completo no puede exceder los 255 caracteres")
    private String fullName;

    // No incluimos id, status, createdAt, updatedAt porque son generados/manejados
    // por el servidor al crear el usuario.
}
