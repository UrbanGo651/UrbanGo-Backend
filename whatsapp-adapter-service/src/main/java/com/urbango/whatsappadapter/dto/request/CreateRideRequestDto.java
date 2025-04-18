package com.urbango.whatsappadapter.dto.request;

import com.urbango.whatsappadapter.enums.ServiceType; // <<< Importa el Enum local
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

/**
 * DTO para construir la solicitud de creación de viaje a RideServiceClient.
 * NO incluye validaciones aquí; se validan en rides-service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRideRequestDto {

    private UUID userId; // ID del usuario que solicita
    private ServiceType serviceType; // Usa el Enum local

    // Campos opcionales que el adaptador podría rellenar
    private String originDetails;
    private String destinationDetails;
}
