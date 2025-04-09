package com.urbango.service.impl;

import com.urbango.dto.DriverRequestDto;
import com.urbango.dto.DriverResponseDto;
import com.urbango.model.Driver;
import com.urbango.respository.DriverRepository;
import com.urbango.service.DriverServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DriverServiceImpl implements DriverServiceInterface {

    @Autowired
    private DriverRepository driverRepository;

    @Override
    public DriverResponseDto registerDriver(DriverRequestDto driverRequestDto) {
        Driver driver = new Driver();
        driver.setName(driverRequestDto.getName());
        driver.setPhone(driverRequestDto.getPhone());
        driver.setVehicleType(driverRequestDto.getVehicleType());
        driver.setStatus("disponible"); // Nuevo conductor es disponible inicialmente

        // Asignamos los nuevos campos:
        driver.setLicenseNumber(driverRequestDto.getLicenseNumber());
        driver.setSoatValid(driverRequestDto.getSoatValid());
        driver.setTecnomecanicaValid(driverRequestDto.getTecnomecanicaValid());
        driver.setJudicialRecordsValid(driverRequestDto.getJudicialRecordsValid());
        driver.setVehiclePlate(driverRequestDto.getVehiclePlate());

        Driver savedDriver = driverRepository.save(driver);
        return mapToDto(savedDriver);
    }

    @Override
    public DriverResponseDto updateStatus(Long driverId, String status) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        driver.setStatus(status);
        Driver savedDriver = driverRepository.save(driver);
        return mapToDto(savedDriver);
    }

    @Override
    public List<DriverResponseDto> getAvailableDrivers(String vehicleType) {
        List<Driver> drivers = driverRepository.findByVehicleTypeAndStatus(vehicleType, "disponible");
        return drivers.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Driver getDriverById(Long driverId) {
        return driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
    }

    // Metodo privado para mapear la entidad a DTO
    private DriverResponseDto mapToDto(Driver driver) {
        DriverResponseDto dto = new DriverResponseDto();
        dto.setId(driver.getId());
        dto.setName(driver.getName());
        dto.setPhone(driver.getPhone());
        dto.setVehicleType(driver.getVehicleType());
        dto.setStatus(driver.getStatus());
        dto.setCreatedAt(driver.getCreatedAt());
        dto.setLicenseNumber(driver.getLicenseNumber());
        dto.setSoatValid(driver.getSoatValid());
        dto.setTecnomecanicaValid(driver.getTecnomecanicaValid());
        dto.setJudicialRecordsValid(driver.getJudicialRecordsValid());
        dto.setVehiclePlate(driver.getVehiclePlate());
        return dto;
    }
}
