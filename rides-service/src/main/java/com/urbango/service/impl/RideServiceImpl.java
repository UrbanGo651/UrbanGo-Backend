package com.urbango.service.impl;

import com.urbango.client.NotificationClient;
import com.urbango.dto.NotificationRequestDto;
import com.urbango.dto.RideAssignmentResponse;
import com.urbango.dto.RideRequestDto;
import com.urbango.dto.RideResponseDto;
import com.urbango.model.Ride;
import com.urbango.respository.RideRepository;
import com.urbango.service.RideServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class RideServiceImpl implements RideServiceInterface {

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private NotificationClient notificationClient;

    @Override
    public RideResponseDto createRide(RideRequestDto rideRequestDto) {
        Ride ride = new Ride();
        ride.setType(rideRequestDto.getType());
        ride.setStatus("pending");
        ride.setRequestTime(LocalDateTime.now());
        Ride savedRide = rideRepository.save(ride);

        // Notificación a grupo correspondiente
        NotificationRequestDto notification = new NotificationRequestDto();
        notification.setGroupType(ride.getType()); // "carro", "moto", "domicilio"
        notification.setMessage("🚨 Nueva solicitud de " + ride.getType() + " con ID #" + savedRide.getId() +
                ". Responde con 'acepto " + savedRide.getId() + "' para tomarla.");

        notificationClient.sendNotification(notification);

        return mapToDto(savedRide);
    }

    @Override
    public RideAssignmentResponse acceptRide(Long rideId, Long driverId) {
        Optional<Ride> optionalRide = rideRepository.findById(rideId);
        if (optionalRide.isEmpty()) {
            throw new RuntimeException("Ride no encontrado");
        }
        Ride ride = optionalRide.get();
        if (ride.getDriverId() != null) {
            // El viaje ya fue asignado
            throw new RuntimeException("Viaje ya fue asignado a otro conductor");
        }

        // Simulación: obtenemos datos del conductor (esto se reemplazará con una llamada real al drivers-service)
        String driverName = "Juan Pérez";
        String vehiclePlate = "ABC123";
        String vehicleModel = "Toyota Corolla";
        String driverWhatsAppLink = "https://wa.me/573116534460";

        // Asignar el conductor al ride
        ride.setDriverId(driverId);
        ride.setStatus("ASIGNADO");
        rideRepository.save(ride);

        // Construir la respuesta
        RideAssignmentResponse response = new RideAssignmentResponse();
        response.setRideId(ride.getId());
        response.setDriverId(driverId.toString());
        response.setDriverName(driverName);
        response.setVehiclePlate(vehiclePlate);
        response.setVehicleModel(vehicleModel);
        response.setDriverWhatsAppLink(driverWhatsAppLink);

        return response;
    }

    @Override
    public RideResponseDto assignDriver(Long rideId, Long driverId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));
        if ("pending".equals(ride.getStatus())) {
            ride.setDriverId(driverId);
            ride.setStatus("assigned");
            Ride savedRide = rideRepository.save(ride);
            return mapToDto(savedRide);
        }
        throw new IllegalStateException("Ride already assigned or completed");
    }

    @Override
    public RideResponseDto completeRide(Long rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));
        ride.setStatus("completed");
        Ride savedRide = rideRepository.save(ride);
        return mapToDto(savedRide);
    }

    // Método privado para mapear de Entity a DTO
    private RideResponseDto mapToDto(Ride ride) {
        RideResponseDto dto = new RideResponseDto();
        dto.setId(ride.getId());
        dto.setType(ride.getType());
        dto.setStatus(ride.getStatus());
        dto.setRequestTime(ride.getRequestTime());
        dto.setDriverId(ride.getDriverId());
        return dto;
    }
}
