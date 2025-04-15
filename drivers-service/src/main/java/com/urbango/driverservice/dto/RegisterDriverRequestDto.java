package com.urbango.driverservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDriverRequestDto {

    @NotBlank(message = "El número de WhatsApp no puede estar vacío")
    @Pattern(regexp = "^\\+57\\d{10}$", message = "El formato del número de WhatsApp debe ser +57 seguido de 10 dígitos (ej: +573001234567)")
    private String whatsappNumber;

    @NotBlank(message = "El nombre completo no puede estar vacío")
    @Size(max = 255, message = "El nombre completo no puede exceder los 255 caracteres")
    private String fullName;

    // Podríamos añadir el tipo de vehículo inicial aquí si el flujo lo requiere
    // @NotBlank(message = "El tipo de vehículo inicial es requerido")
    // private String initialVehicleType; // Ej: "CAR", "MOTORCYCLE"

    // Nota: La placa, documentos, etc., se añadirán en pasos posteriores,
    // este DTO es solo para el registro inicial básico.
}
