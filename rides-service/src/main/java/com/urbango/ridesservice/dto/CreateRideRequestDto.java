package com.urbango.ridesservice.dto;

import com.urbango.ridesservice.enums.ServiceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRideRequestDto {

    @NotNull(message = "El ID del usuario no puede ser nulo")
    private UUID userId; // Quién solicita el viaje

    @NotNull(message = "El tipo de servicio no puede estar vacío")
    // Podríamos validar contra un Enum aquí también si quisiéramos
    private ServiceType serviceType; // "CAR", "MOTORCYCLE", "DELIVERY"

    // --- Detalles de Ubicación (Textual por ahora) ---
    // Opcionales, ya que la coordinación podría ser directa por WhatsApp
    private String originDetails;
    private String destinationDetails;

    // --- Información adicional que el usuario podría enviar (vía WhatsApp) ---
    // private String additionalNotes; // Ej: "Llevo una maleta grande"
}