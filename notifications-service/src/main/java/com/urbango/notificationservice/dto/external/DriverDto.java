package com.urbango.notificationservice.dto.external;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.UUID;
// No necesitamos importar VehicleDto o DriverDocumentDto aquí si no los usamos directamente

/**
 * Representa la información mínima esperada de un Driver desde DriverServiceClient.
 * Utilizado para deserializar la respuesta del Feign Client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverDto {

    // ID es útil para confirmar que se obtuvo el conductor correcto
    private UUID id;

    // Campo ESENCIAL para enviar la notificación
    private String whatsappNumber;

    // Campo útil para personalizar mensajes
    private String fullName;

    // No necesitamos otros campos como driverStatus, vehicles, documents, etc.,
    // a menos que un mensaje específico los requiera (lo cual es poco común
    // para el servicio de notificación, que generalmente solo necesita el contacto).
    // Si necesitáramos detalles del vehículo (ej. placa para un mensaje),
    // añadiríamos aquí una referencia a un VehicleDto externo.
}
