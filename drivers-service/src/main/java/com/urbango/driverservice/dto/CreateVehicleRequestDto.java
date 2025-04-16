package com.urbango.driverservice.dto;

import com.urbango.driverservice.enums.VehicleType; // Importar Enum
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class CreateVehicleRequestDto {

    @NotBlank(message = "La placa no puede estar vacía")
    @Size(min = 5, max = 10, message = "La placa debe tener entre 5 y 10 caracteres")
    // Podrías añadir un @Pattern más específico para placas colombianas si lo deseas
    private String licensePlate;

    @NotNull(message = "El tipo de vehículo no puede ser nulo")
    private VehicleType vehicleType; // Usamos el Enum

    @Size(max = 100, message = "El modelo no puede exceder los 100 caracteres")
    private String model; // Opcional

    @Size(max = 50, message = "El color no puede exceder los 50 caracteres")
    private String color; // Opcional

    private LocalDate soatExpiryDate; // Opcional

    private LocalDate technoExpiryDate; // Opcional

    // No necesitamos driverId aquí, se obtiene de la URL

}
