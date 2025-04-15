package com.urbango.driverservice.enums;

public enum DriverStatus {

    PENDING_APPROVAL, // Pendiente de aprobación inicial
    ACTIVE_OFFLINE,   // Aprobado, pero no disponible para recibir viajes
    ACTIVE_AVAILABLE, // Aprobado y listo para recibir viajes
    ON_RIDE,          // Actualmente realizando un servicio
    SUSPENDED,        // Temporalmente suspendido
    REJECTED          // Registro rechazado
    //añadir más estados si se necesitas
}
