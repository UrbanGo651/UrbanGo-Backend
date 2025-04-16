package com.urbango.ridesservice.client;

import com.urbango.ridesservice.dto.external.DriverDto; // Usaremos un DTO externo
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

// name: nombre registrado en el servidor de descubrimiento (si usas Eureka/Consul)
// O usa 'url' para apuntar directamente en desarrollo
@FeignClient(name = "driver-service", url = "${services.driver.url:http://localhost:8087}/api/v1/drivers")
public interface DriverServiceClient {

    // Endpoint para buscar conductores disponibles
    @GetMapping("/available")
    ResponseEntity<List<DriverDto>> findAvailableDrivers(@RequestParam("vehicleType") String vehicleType); // Recibe String, convierte a Enum internamente si es necesario

    // Endpoint para obtener detalles de un conductor por ID (necesitamos el DTO completo)
    @GetMapping("/{id}/details")
    ResponseEntity<DriverDto> getDriverDetailsById(@PathVariable("id") UUID driverId);

    // Endpoint para actualizar el estado de un conductor
    @PatchMapping("/{id}/status")
    ResponseEntity<DriverDto> updateDriverStatus(@PathVariable("id") UUID driverId, @RequestBody Map<String, String> statusUpdate);
}
