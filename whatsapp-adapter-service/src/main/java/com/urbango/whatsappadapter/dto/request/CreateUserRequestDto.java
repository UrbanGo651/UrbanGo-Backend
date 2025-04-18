package com.urbango.whatsappadapter.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO usado por WhatsappAdapterService para construir la solicitud
 * que se enviará a UserServiceClient.createUser.
 * No necesita validaciones aquí.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequestDto {
    private String whatsappNumber; // Debe estar en formato E.164 (+57...)
    private String fullName;
}
