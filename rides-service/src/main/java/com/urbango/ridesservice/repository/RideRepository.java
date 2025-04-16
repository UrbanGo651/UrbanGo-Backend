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

    // Buscar viajes por el ID del usuario
    List<Ride> findByUserId(UUID userId);

    // Buscar viajes por el ID del conductor asignado
    List<Ride> findByAssignedDriverId(UUID driverId);

    // Buscar viajes por estado
    List<Ride> findByRideStatus(RideStatus status);

    // Buscar viajes por usuario Y estado
    List<Ride> findByUserIdAndRideStatus(UUID userId, RideStatus status);

    // Buscar viajes por conductor Y estado
    List<Ride> findByAssignedDriverIdAndRideStatus(UUID driverId, RideStatus status);

    // Buscar un viaje activo (no completado/cancelado) para un usuario específico
    // Útil para saber si un usuario ya tiene un viaje en curso.
    // Definimos "activo" como REQUESTED o ASSIGNED (o podrías incluir otros estados en curso)
    Optional<Ride> findFirstByUserIdAndRideStatusIn(UUID userId, List<RideStatus> activeStatuses);

    // Buscar un viaje activo para un conductor específico
    Optional<Ride> findFirstByAssignedDriverIdAndRideStatusIn(UUID driverId, List<RideStatus> activeStatuses);


    // Los métodos CRUD básicos (save, findById, findAll, deleteById)
    // ya están proporcionados por JpaRepository.
}
