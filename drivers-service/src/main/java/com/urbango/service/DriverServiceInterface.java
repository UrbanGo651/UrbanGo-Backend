package com.urbango.service;

import com.urbango.dto.DriverRequestDto;
import com.urbango.dto.DriverResponseDto;
import com.urbango.model.Driver;

import java.util.List;

public interface DriverServiceInterface {
    DriverResponseDto registerDriver(DriverRequestDto driverRequestDto);
    DriverResponseDto updateStatus(Long driverId, String status);
    List<DriverResponseDto> getAvailableDrivers(String vehicleType);
    Driver getDriverById(Long driverId);
}
