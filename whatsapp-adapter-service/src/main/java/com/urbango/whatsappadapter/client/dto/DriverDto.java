package com.urbango.whatsappadapter.client.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.ArrayList; // Importar para inicializar la lista
import java.util.List;    // Importar List
import java.util.UUID;
// No necesitamos Instant aquí si no lo usamos directamente

/**
 * Representa la información esperada de un Driver desde DriverServiceClient.
 * Utilizado para deserializar la respuesta del Feign Client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverDto {

    // Necesario para identificar al conductor internamente
    private UUID id;

    // Necesario para identificar y potencialmente responder
    private String whatsappNumber;

    // Útil para mensajes personalizados
    private String fullName;

    // El estado es útil para saber si puede aceptar viajes, etc.
    private String driverStatus; // Mantenido como String para simplificar la comunicación entre servicios

    // La lista de vehículos es necesaria para el método findActiveVehicleId
    private List<VehicleDto> vehicles = new ArrayList<>(); // <<< Incluir la lista

    // No necesitamos documentos aquí por ahora
    // private List<DriverDocumentDto> documents = new ArrayList<>();
    // private java.time.Instant approvalTimestamp;
    // private java.time.Instant createdAt;
    // private java.time.Instant updatedAt;
}
