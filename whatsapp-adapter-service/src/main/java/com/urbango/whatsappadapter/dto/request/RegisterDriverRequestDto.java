package com.urbango.whatsappadapter.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO para construir la solicitud de registro de conductor a DriverServiceClient.
 * NO incluye validaciones aquí; se validan en drivers-service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDriverRequestDto {
    private String whatsappNumber; // Debe estar en formato +57XXXXXXXXXX
    private String fullName;
}
