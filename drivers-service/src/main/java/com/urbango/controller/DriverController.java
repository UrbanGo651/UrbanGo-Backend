package com.urbango.controller;

import com.urbango.dto.DriverRequestDto;
import com.urbango.dto.DriverResponseDto;
import com.urbango.service.DriverServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/drivers")
public class DriverController {

    @Autowired
    private DriverServiceInterface driverService;

    // Endpoint para registrar un conductor con todos los datos requeridos
    @PostMapping("/register")
    public DriverResponseDto registerDriver(@RequestBody DriverRequestDto driverRequestDto) {
        return driverService.registerDriver(driverRequestDto);
    }

    // Endpoint para actualizar el estado de un conductor
    @PutMapping("/{driverId}/status")
    public DriverResponseDto updateStatus(@PathVariable Long driverId, @RequestParam String status) {
        return driverService.updateStatus(driverId, status);
    }

    // Endpoint para obtener conductores disponibles según el tipo de vehículo
    @GetMapping("/available")
    public List<DriverResponseDto> getAvailableDrivers(@RequestParam String vehicleType) {
        return driverService.getAvailableDrivers(vehicleType);
    }
}
