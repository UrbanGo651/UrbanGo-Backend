package com.urbango.ridesservice.enums;

public enum RideStatus {

    REQUESTED,        // Solicitud creada por el usuario, buscando conductor
    ASSIGNED,         // Conductor aceptó y fue asignado
    // EN_ROUTE_TO_PICKUP, // (Opcional) Conductor en camino a recoger
    // AT_PICKUP,          // (Opcional) Conductor llegó al punto de recogida
    // ONGOING,         // (Opcional) Viaje en curso
    COMPLETED,        // Viaje finalizado exitosamente
    CANCELLED_USER,   // Cancelado por el usuario
    CANCELLED_DRIVER, // Cancelado por el conductor (antes o después de aceptar)
    TIMEOUT_NO_DRIVER // No se encontró conductor en un tiempo razonable
    // Puedes refinar estos estados según tu lógica
}
