package com.urbango.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private UUID id;
    private String whatsappNumber;
    private String fullName;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    // Nota: No necesitamos anotaciones de validación aquí, ya que este DTO
    // representa datos que *salen* del servidor, no que entran.
}
