package com.urbango.driverservice.repository;

import com.urbango.driverservice.entity.Driver;
import com.urbango.driverservice.enums.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DriverRepository extends JpaRepository<Driver, UUID> {

    // Buscar por número de WhatsApp (debe ser único)
    Optional<Driver> findByWhatsappNumber(String whatsappNumber);

    // Buscar conductores por estado (útil para encontrar disponibles)
    List<Driver> findByDriverStatus(DriverStatus driverStatus);

    // Podríamos añadir búsquedas más complejas en el futuro, por ejemplo,
    // conductores activos y con cierto tipo de vehículo:
    // List<Driver> findByDriverStatusAndVehicles_VehicleType(String status, String vehicleType);
    // (Esto requiere cuidado con el rendimiento y las queries generadas)
}
