package com.urbango.whatsappadapter.client;

import com.urbango.whatsappadapter.client.dto.DriverDto; // DTO externo
import com.urbango.whatsappadapter.dto.request.RegisterDriverRequestDto; // DTO de driver-service (o copia local)
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "drivers-service", url = "${services.driver.url}/api/v1/drivers")
public interface DriverServiceClient {

    // Para identificar remitente
    @GetMapping("/whatsapp/{whatsappNumber}")
    ResponseEntity<DriverDto> findDriverByWhatsappNumber(@PathVariable("whatsappNumber") String whatsappNumber);

    // Para registrar nuevo conductor
    @PostMapping
    ResponseEntity<DriverDto> registerDriver(@RequestBody RegisterDriverRequestDto requestDto); // Necesitamos este DTO

    // Para obtener detalles (ej. vehículos para aceptar viaje)
    @GetMapping("/{id}/details")
    ResponseEntity<DriverDto> getDriverDetailsById(@PathVariable("id") UUID driverId);

    // Para actualizar estado (disponible/no disponible)
    @PatchMapping("/{id}/status")
    ResponseEntity<DriverDto> updateDriverStatus(@PathVariable("id") UUID driverId, @RequestBody Map<String, String> statusUpdate);

}
