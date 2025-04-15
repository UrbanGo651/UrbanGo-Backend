package com.urbango.driverservice.repository;

import com.urbango.driverservice.entity.Vehicle;
import com.urbango.driverservice.enums.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    //Buscar vehículo por placa (debe ser única)
    Optional<Vehicle> findByLicensePlate(String licensePlate);

    // Buscar todos los vehículos de un conductor específico
    List<Vehicle> findByDriverId(UUID driverId);

    // Buscar vehículos de un conductor por tipo
    List<Vehicle> findByDriverIdAndVehicleType(UUID driverId, VehicleType vehicleType);
}
