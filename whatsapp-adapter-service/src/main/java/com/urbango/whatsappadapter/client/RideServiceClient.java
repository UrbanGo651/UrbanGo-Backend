package com.urbango.whatsappadapter.client;

import com.urbango.whatsappadapter.client.dto.RideDto; // DTO externo
import com.urbango.whatsappadapter.dto.request.CreateRideRequestDto; // DTO de ride-service (o copia local)
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "rides-service", url = "${services.ride.url}/api/v1/rides")
public interface RideServiceClient {
    // Para solicitar un viaje
    @PostMapping
    ResponseEntity<RideDto> requestRide(@RequestBody CreateRideRequestDto requestDto); // Necesitamos este DTO

    // Para aceptar un viaje
    @PostMapping("/{rideId}/accept")
    ResponseEntity<RideDto> acceptRide(@PathVariable("rideId") UUID rideId, @RequestBody Map<String, String> acceptRequest);

    // Para completar un viaje
    @PostMapping("/{rideId}/complete")
    ResponseEntity<RideDto> completeRide(@PathVariable("rideId") UUID rideId, @RequestBody Map<String, String> completeRequest);

    // Para obtener detalles si fuera necesario (ej. para convertir ID corto)
    @GetMapping("/{rideId}")
    ResponseEntity<RideDto> getRideById(@PathVariable("rideId") UUID rideId);

    // --- MÉTODO PARA BUSCAR POR SHORT ID ---
    @GetMapping("/short/{shortId}") // <<< ASEGÚRATE DE QUE ESTE MÉTODO ESTÉ DEFINIDO
    ResponseEntity<RideDto> findRideByShortId(@PathVariable("shortId") String shortId);
}
