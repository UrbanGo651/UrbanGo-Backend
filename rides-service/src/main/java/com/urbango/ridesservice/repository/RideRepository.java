package com.urbango.ridesservice.repository;
import com.urbango.ridesservice.entity.Ride;
import com.urbango.ridesservice.enums.RideStatus; // Importar Enum
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.Optional; // Aunque findById ya lo devuelve, por claridad

@Repository
public interface RideRepository extends JpaRepository<Ride, UUID> {

    // --- Búsquedas por ID ---
    // findById(UUID id) // Ya viene de JpaRepository

    // *** NUEVO: Búsqueda eficiente por Short ID ***
    Optional<Ride> findByShortId(String shortId);

    // --- Otras búsquedas (sin cambios) ---
    List<Ride> findByUserId(UUID userId);
    List<Ride> findByAssignedDriverId(UUID driverId);
    List<Ride> findByRideStatus(RideStatus status);
    List<Ride> findByUserIdAndRideStatus(UUID userId, RideStatus status);
    List<Ride> findByAssignedDriverIdAndRideStatus(UUID driverId, RideStatus status);
    Optional<Ride> findFirstByUserIdAndRideStatusIn(UUID userId, List<RideStatus> activeStatuses);
    Optional<Ride> findFirstByAssignedDriverIdAndRideStatusIn(UUID driverId, List<RideStatus> activeStatuses);

    // Ya NO necesitamos los métodos con LIKE y prefijo/sufijo
    // List<Ride> findByShortIdPrefix(String shortIdPrefix);
    // List<Ride> findByShortIdSuffix(String shortIdSuffix);
}
