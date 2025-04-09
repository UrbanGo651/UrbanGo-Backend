package com.urbango.controller;

import com.urbango.dto.RideAcceptanceRequest;
import com.urbango.dto.RideAssignmentResponse;
import com.urbango.dto.RideRequestDto;
import com.urbango.dto.RideResponseDto;
import com.urbango.service.RideServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rides")
public class RideController {
    @Autowired
    private RideServiceInterface rideService;

    // Endpoint para crear una solicitud de ride
    @PostMapping("/request")
    public RideResponseDto requestRide(@RequestBody RideRequestDto rideRequestDto) {
        return rideService.createRide(rideRequestDto);
    }

    @PostMapping("/accept-ride")
    public ResponseEntity<?> acceptRide(@RequestBody RideAcceptanceRequest request) {
        try {
            RideAssignmentResponse response = rideService.acceptRide(request.getRideId(), request.getDriverId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Puedes retornar conflicto (409) o error (400) según la naturaleza del problema
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }


    // Endpoint para asignar un conductor a una solicitud
    @PostMapping("/{rideId}/assign")
    public RideResponseDto assignRide(@PathVariable Long rideId, @RequestParam Long driverId) {
        return rideService.assignDriver(rideId, driverId);
    }

    // Endpoint para finalizar un ride
    @PostMapping("/{rideId}/complete")
    public RideResponseDto completeRide(@PathVariable Long rideId) {
        return rideService.completeRide(rideId);
    }
}

