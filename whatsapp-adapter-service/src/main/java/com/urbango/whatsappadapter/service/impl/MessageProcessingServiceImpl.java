package com.urbango.whatsappadapter.service.impl;

import com.urbango.whatsappadapter.client.DriverServiceClient;
import com.urbango.whatsappadapter.client.RideServiceClient;
import com.urbango.whatsappadapter.client.UserServiceClient;
import com.urbango.whatsappadapter.client.dto.DriverDto;
import com.urbango.whatsappadapter.client.dto.RideDto;
import com.urbango.whatsappadapter.client.dto.UserDto;
import com.urbango.whatsappadapter.client.dto.VehicleDto;
import com.urbango.whatsappadapter.dto.request.CreateRideRequestDto;
import com.urbango.whatsappadapter.dto.request.CreateUserRequestDto;
import com.urbango.whatsappadapter.dto.request.RegisterDriverRequestDto;
import com.urbango.whatsappadapter.enums.ServiceType;
import com.urbango.whatsappadapter.service.MessageProcessingService;
import com.urbango.whatsappadapter.service.WhatsAppResponseService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageProcessingServiceImpl implements MessageProcessingService {

    private final UserServiceClient userServiceClient;
    private final DriverServiceClient driverServiceClient;
    private final RideServiceClient rideServiceClient;
    private final WhatsAppResponseService responseService;

    // Patrones para comandos con ID corto al final
    private static final Pattern ACCEPT_PATTERN = Pattern.compile("acepto\\s+([a-fA-F0-9]{8})$");
    private static final Pattern COMPLETE_PATTERN = Pattern.compile("finalizado\\s+([a-fA-F0-9]{8})$");

    // Estado de conversación simple en memoria (MEJORAR PARA PRODUCCIÓN)
    private static final Map<String, String> conversationState = new ConcurrentHashMap<>();
    private static final String STATE_WAITING_USERNAME = "WAITING_USERNAME";
    private static final String STATE_WAITING_DRIVERNAME = "WAITING_DRIVERNAME";

    @Override
    public void processIncomingMessage(String fromNumber, String toNumber, String messageBody) {
        String normalizedFromNumber = normalizePhoneNumber(fromNumber);
        if (normalizedFromNumber == null) {
            log.error("Número de remitente inválido: {}", fromNumber);
            return;
        }
        log.info("Procesando mensaje de {} : '{}'", normalizedFromNumber, messageBody);
        String command = messageBody.trim().toLowerCase(); // Comando normalizado

        // Identificar Remitente
        Optional<UserDto> userOpt = findUser(normalizedFromNumber);
        Optional<DriverDto> driverOpt = findDriver(normalizedFromNumber);
        boolean isUserRegistered = userOpt.isPresent();
        boolean isDriverRegistered = driverOpt.isPresent();

        log.debug("Identificación para {}: Usuario={}, Conductor={}", normalizedFromNumber, isUserRegistered, isDriverRegistered);

        // Procesar mensaje según el remitente o estado de conversación
        try {
            if (isUserRegistered) {
                handleUserMessage(userOpt.get(), command);
            } else if (isDriverRegistered) {
                handleDriverMessage(driverOpt.get(), command);
            } else {
                handleNewOrRegisteringContact(normalizedFromNumber, messageBody); // Pasar body original
            }
        } catch (Exception e) {
            log.error("Error crítico procesando mensaje de {}: {}", normalizedFromNumber, e.getMessage(), e);
            sendResponse(normalizedFromNumber, "Lo siento, ocurrió un error interno inesperado. Por favor, informa al administrador.");
        }
    }

    // --- Manejador para Nuevos Contactos o en Proceso de Registro ---
    private void handleNewOrRegisteringContact(String fromNumber, String messageBody) {
        String currentState = conversationState.get(fromNumber);
        String command = messageBody.toLowerCase();
        String receivedName = messageBody; // Usar el texto original para el nombre

        if (STATE_WAITING_USERNAME.equals(currentState)) {
            // Proceso de registro de usuario: Recibió el nombre
            log.debug("Recibido nombre de usuario de {}: {}", fromNumber, receivedName);
            conversationState.remove(fromNumber); // Limpiar estado
            CreateUserRequestDto newUserDto = new CreateUserRequestDto(fromNumber, receivedName);
            try {
                ResponseEntity<UserDto> response = userServiceClient.createUser(newUserDto);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    log.info("Usuario registrado: {} - ID: {}", fromNumber, response.getBody().getId());
                    // Saludo usando solo el primer nombre
                    String firstName = receivedName.split(" ")[0];
                    sendResponse(fromNumber, "¡Hola " + firstName + "! 👍 Registro completo. Ya puedes pedir servicios escribiendo: *MOTO*, *CARRO* o *DOMICILIO*.");
                } else {
                    log.error("Error en respuesta de createUser para {}: {}", fromNumber, response.getStatusCode());
                    sendResponse(fromNumber, "Hubo un problema al guardar tu registro (Cod: " + response.getStatusCodeValue() + "). Intenta enviar tu nombre de nuevo.");
                    conversationState.put(fromNumber, STATE_WAITING_USERNAME); // Reintentar pedir nombre
                }
            } catch (FeignException e) {
                handleFeignException(e, fromNumber, "registrarte como usuario");
                conversationState.remove(fromNumber); // Limpiar estado en error Feign también
            } catch (Exception e){
                log.error("Error inesperado registrando usuario {}: {}", fromNumber, e.getMessage(), e);
                sendResponse(fromNumber, "Error interno al registrarte. Intenta más tarde.");
                conversationState.remove(fromNumber);
            }
        } else if (STATE_WAITING_DRIVERNAME.equals(currentState)) {
            // Proceso de registro de conductor: Recibió el nombre
            log.debug("Recibido nombre de conductor de {}: {}", fromNumber, receivedName);
            conversationState.remove(fromNumber);
            RegisterDriverRequestDto newDriverDto = new RegisterDriverRequestDto(fromNumber, receivedName);
            try {
                ResponseEntity<DriverDto> response = driverServiceClient.registerDriver(newDriverDto);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    log.info("Conductor pre-registrado: {} - ID: {}", fromNumber, response.getBody().getId());
                    String firstName = receivedName.split(" ")[0];
                    sendResponse(fromNumber, "¡Hola " + firstName + "! ✅ Registro inicial listo. Un administrador revisará tus datos pronto. Recibirás un mensaje cuando seas *aprobado*. Después, escribe *Disponible* para empezar a recibir servicios.");
                } else {
                    log.error("Error en respuesta de registerDriver para {}: {}", fromNumber, response.getStatusCode());
                    sendResponse(fromNumber, "Hubo un problema con tu registro inicial (Cod: " + response.getStatusCodeValue() + "). Intenta enviar tu nombre de nuevo.");
                    conversationState.put(fromNumber, STATE_WAITING_DRIVERNAME); // Reintentar pedir nombre
                }
            } catch (FeignException e) {
                handleFeignException(e, fromNumber, "registrarte como conductor");
                conversationState.remove(fromNumber);
            } catch (Exception e) {
                log.error("Error inesperado registrando conductor {}: {}", fromNumber, e.getMessage(), e);
                sendResponse(fromNumber, "Error interno al registrarte. Intenta más tarde.");
                conversationState.remove(fromNumber);
            }
        } else {
            // Nuevo contacto sin estado previo
            handleWelcomeAndRoleSelection(fromNumber, command);
        }
    }

    // --- Manejador para Mensajes de Usuarios Registrados ---
    private void handleUserMessage(UserDto user, String command) {
        ServiceType serviceType = null;
        if (command.contains("moto")) serviceType = ServiceType.MOTORCYCLE;
        else if (command.contains("carro") || command.contains("coche")) serviceType = ServiceType.CAR;
        else if (command.contains("domicilio")) serviceType = ServiceType.DELIVERY;

        if (serviceType != null) {
            log.info("Usuario {} ({}) solicitando servicio {}", user.getId(), user.getFullName(), serviceType);
            String origin = extractOriginFromMessage(command); // Extraer origen si es posible
            CreateRideRequestDto request = new CreateRideRequestDto(user.getId(), serviceType, origin, null); // Destino se coordina después
            try {
                ResponseEntity<RideDto> response = rideServiceClient.requestRide(request);
                if (response.getStatusCode().is2xxSuccessful()) {
                    sendResponse(user.getWhatsappNumber(), "¡Listo!  searching Buscando un conductor de *" + serviceType + "* para ti... ⏳");
                } else {
                    log.error("Error en respuesta de requestRide para user {}: {}", user.getId(), response.getStatusCode());
                    sendResponse(user.getWhatsappNumber(), "Ups, tuvimos un problema al crear tu solicitud (Cod: " + response.getStatusCodeValue() + ").");
                }
            } catch (FeignException e) {
                handleFeignException(e, user.getWhatsappNumber(), "solicitar tu viaje");
            } catch (Exception e) {
                log.error("Error inesperado en requestRide para user {}: {}", user.getId(), e.getMessage(), e);
                sendResponse(user.getWhatsappNumber(), "Error interno al solicitar tu viaje.");
            }
        } else {
            // Comando no reconocido
            sendResponse(user.getWhatsappNumber(), "Hola " + user.getFullName().split(" ")[0] + "! 👋 Para pedir un servicio, escribe *MOTO*, *CARRO* o *DOMICILIO*.");
        }
    }

    // --- Manejador para Mensajes de Conductores Registrados ---
    private void handleDriverMessage(DriverDto driver, String command) {
        Matcher acceptMatcher = ACCEPT_PATTERN.matcher(command);
        Matcher completeMatcher = COMPLETE_PATTERN.matcher(command);

        if (acceptMatcher.matches()) {
            String rideIdShort = acceptMatcher.group(1);
            log.info("Conductor {} ({}) aceptando viaje ID corto {}", driver.getId(), driver.getFullName(), rideIdShort);
            processAcceptRide(driver, rideIdShort);
        } else if (completeMatcher.matches()) {
            String rideIdShort = completeMatcher.group(1);
            log.info("Conductor {} ({}) finalizando viaje ID corto {}", driver.getId(), driver.getFullName(), rideIdShort);
            processCompleteRide(driver, rideIdShort);
        } else if ("estoy disponible".equals(command) || "disponible".equals(command) || "activo".equals(command) || "activarme".equals(command)) {
            processDriverAvailability(driver, true); // Poner disponible
        } else if (command.startsWith("no disponible") || "ocupado".equals(command) || "offline".equals(command) || "desactivarme".equals(command)) {
            processDriverAvailability(driver, false); // Poner no disponible
        } else {
            sendResponse(driver.getWhatsappNumber(), "No entendí tu comando 🤔. Opciones: *Acepto ID*, *Finalizado ID*, *Disponible*, *No disponible*.");
        }
    }

    private void handleWelcomeAndRoleSelection(String fromNumber, String command) {
        // Verificar si es un comando de solicitud de rol explícito
        if ("usuario".equals(command) || "1".equals(command)) {
            // El usuario ya indicó su rol
            conversationState.put(fromNumber, STATE_WAITING_USERNAME); // Poner en estado de espera de nombre
            sendResponse(fromNumber, "¡Excelente! 👍 Para crear tu cuenta de usuario, por favor dime tu *nombre completo*:");
        } else if ("conductor".equals(command) || "2".equals(command)) {
            // El usuario ya indicó su rol
            conversationState.put(fromNumber, STATE_WAITING_DRIVERNAME); // Poner en estado de espera de nombre
            sendResponse(fromNumber, "¡Genial!  Fahrer Para iniciar tu registro como conductor, por favor dime tu *nombre completo*:");
        } else {
            // Es el primer saludo o un comando no reconocido en este contexto
            log.debug("Enviando mensaje de bienvenida/selección de rol a {}", fromNumber);
            sendResponse(fromNumber, "¡Hola! 👋 Bienvenido a UrbanGo 🛵🚗💨. Para continuar, dime si eres:\n1. *Usuario*\n2. *Conductor*");
            // No ponemos estado aquí, esperamos que respondan "Usuario" o "Conductor"
        }
    }

    // --- Lógica de Acciones Específicas ---

    private void processAcceptRide(DriverDto driver, String rideIdShort) {
        // Validar estado actual del conductor (para evitar aceptar si está offline o en otro viaje)
        if (!"ACTIVE_AVAILABLE".equalsIgnoreCase(driver.getDriverStatus())) {
            log.warn("Conductor {} intentó aceptar viaje {} pero no está disponible (estado: {})", driver.getId(), rideIdShort, driver.getDriverStatus());
            sendResponse(driver.getWhatsappNumber(), "No puedes aceptar servicios ahora. Asegúrate de estar en estado *DISPONIBLE*.");
            return;
        }

        UUID rideId = findRideIdFromShort(rideIdShort, driver.getWhatsappNumber());
        if (rideId == null) return;

        Optional<UUID> vehicleIdOpt = findActiveVehicleId(driver.getId(), driver.getWhatsappNumber());
        if (vehicleIdOpt.isEmpty()) return;

        Map<String, String> acceptRequest = Map.of(
                "driverId", driver.getId().toString(),
                "vehicleId", vehicleIdOpt.get().toString()
        );
        try {
            rideServiceClient.acceptRide(rideId, acceptRequest);
            log.info("Llamada acceptRide enviada para viaje {} por conductor {}", rideId, driver.getId());
            // NO enviamos respuesta aquí. Notification-service se encarga.
        } catch (FeignException e) {
            // Manejar errores específicos devueltos por acceptRide
            handleFeignException(e, driver.getWhatsappNumber(), "aceptar el viaje " + rideIdShort, Map.of(
                    HttpStatus.CONFLICT, "Este viaje ("+rideIdShort+"...) ya fue asignado a otro conductor o no está disponible.",
                    HttpStatus.NOT_FOUND, "El viaje ("+rideIdShort+"...) que intentaste aceptar ya no existe."
            ));
        } catch (Exception e) {
            log.error("Error inesperado en acceptRide para viaje {}: {}", rideId, e.getMessage(), e);
            sendResponse(driver.getWhatsappNumber(), "Error interno al aceptar el viaje ("+rideIdShort+").");
        }
    }

    private void processCompleteRide(DriverDto driver, String rideIdShort) {
        UUID rideId = findRideIdFromShort(rideIdShort, driver.getWhatsappNumber());
        if (rideId == null) return;

        Map<String, String> completeRequest = Map.of("driverId", driver.getId().toString());
        try {
            ResponseEntity<RideDto> response = rideServiceClient.completeRide(rideId, completeRequest);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Viaje {} completado exitosamente por {}", rideId, driver.getId());
                sendResponse(driver.getWhatsappNumber(), "✅ ¡Viaje ("+rideIdShort+"...) finalizado correctamente! Ya estás listo para nuevos servicios si te pones *Disponible*.");
            } else {
                // Esto indica un error inesperado en la lógica del servicio de rides
                log.error("Llamada completeRide para viaje {} falló inesperadamente: {}", rideId, response.getStatusCode());
                sendResponse(driver.getWhatsappNumber(), "No se pudo finalizar el viaje " + rideIdShort + "... (Cod: " + response.getStatusCodeValue() + "). Contacta a soporte.");
            }
        } catch (FeignException e) {
            handleFeignException(e, driver.getWhatsappNumber(), "finalizar el viaje " + rideIdShort, Map.of(
                    HttpStatus.NOT_FOUND, "El viaje ("+rideIdShort+"...) no existe.",
                    HttpStatus.FORBIDDEN, "No puedes finalizar este viaje. Verifica si te fue asignado o si ya finalizó.",
                    HttpStatus.CONFLICT, "Este viaje ya finalizó o fue cancelado." // Asumiendo 409 para estados finales
            ));
        } catch (Exception e) {
            log.error("Error inesperado en completeRide para viaje {}: {}", rideId, e.getMessage(), e);
            sendResponse(driver.getWhatsappNumber(), "Error interno al finalizar el viaje ("+rideIdShort+").");
        }
    }

    private void processDriverAvailability(DriverDto driver, boolean available) {
        String targetStatus = available ? "ACTIVE_AVAILABLE" : "ACTIVE_OFFLINE";
        String currentStatus = driver.getDriverStatus(); // Asume que DriverDto lo incluye

        // Evitar llamadas innecesarias si ya está en el estado deseado
        if (targetStatus.equalsIgnoreCase(currentStatus)) {
            sendResponse(driver.getWhatsappNumber(), "Ya te encuentras en estado *" + targetStatus.replace("ACTIVE_", "").replace("_", " ") + "*.");
            return;
        }

        // Validar si puede cambiar de estado (simplificado)
        if (!available && "PENDING_APPROVAL".equalsIgnoreCase(currentStatus)) {
            sendResponse(driver.getWhatsappNumber(), "Aún estás pendiente de aprobación.");
            return;
        }
        if (!available && ("SUSPENDED".equalsIgnoreCase(currentStatus) || "REJECTED".equalsIgnoreCase(currentStatus))) {
            sendResponse(driver.getWhatsappNumber(), "Tu cuenta no está activa.");
            return;
        }
        if (available && ("PENDING_APPROVAL".equalsIgnoreCase(currentStatus) || "SUSPENDED".equalsIgnoreCase(currentStatus) || "REJECTED".equalsIgnoreCase(currentStatus))) {
            sendResponse(driver.getWhatsappNumber(), "Tu cuenta debe estar aprobada y activa para ponerte disponible.");
            return;
        }
        // Podríamos añadir lógica para no ponerse disponible si está ON_RIDE, aunque completeRide lo haría.

        updateDriverStatusInternal(driver.getId(), driver.getWhatsappNumber(), targetStatus);
    }


    // --- Métodos Auxiliares (Feign, Normalización, Envío) ---

    private Optional<UserDto> findUser(String whatsappNumber) {
        // ... (Implementación sin cambios, usa Feign y maneja NotFound) ...
        try {
            ResponseEntity<UserDto> response = userServiceClient.findUserByWhatsappNumber(whatsappNumber);
            return (response.getStatusCode() == HttpStatus.OK && response.getBody() != null)
                    ? Optional.of(response.getBody()) : Optional.empty();
        } catch (FeignException.NotFound nf) { return Optional.empty(); }
        catch (Exception e) { log.error("Error buscando user {}: {}", whatsappNumber, e.getMessage()); return Optional.empty(); }
    }

    private Optional<DriverDto> findDriver(String whatsappNumber) {
        // ... (Implementación sin cambios, usa Feign y maneja NotFound) ...
        try {
            ResponseEntity<DriverDto> response = driverServiceClient.findDriverByWhatsappNumber(whatsappNumber);
            // Necesitamos el estado del driver aquí, así que usamos getDriverDetailsById si findByWhatsappNumber no lo devuelve
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null && response.getBody().getId() != null) {
                // Si el DTO devuelto por findByWhatsappNumber ya tiene todo, lo usamos.
                // Si no, podríamos necesitar llamar a getDriverDetailsById aquí.
                // Asumimos que findByWhatsappNumber devuelve suficiente info (ID, Nombre, WhatsApp, Status)
                return Optional.of(response.getBody());
            }
            return Optional.empty();
        } catch (FeignException.NotFound nf) { return Optional.empty(); }
        catch (Exception e) { log.error("Error buscando driver {}: {}", whatsappNumber, e.getMessage()); return Optional.empty(); }
    }

    // Actualizado para manejar errores y enviar respuesta desde aquí
    private void updateDriverStatusInternal(UUID driverId, String driverWhatsapp, String newStatus) {
        Map<String, String> statusUpdate = Map.of("status", newStatus);
        try {
            ResponseEntity<DriverDto> response = driverServiceClient.updateDriverStatus(driverId, statusUpdate);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Estado de driver {} actualizado a {}", driverId, newStatus);
                String statusMsg = newStatus.toLowerCase().replace("active_", "").replace("_", " ");
                sendResponse(driverWhatsapp, "✅ Tu estado ahora es: *" + statusMsg.toUpperCase() + "*");
            } else {
                log.error("Llamada updateDriverStatus para {} falló: {}", driverId, response.getStatusCode());
                sendResponse(driverWhatsapp, "⚠️ No se pudo actualizar tu estado. Intenta de nuevo.");
            }
        } catch (FeignException e) {
            handleFeignException(e, driverWhatsapp, "actualizar tu estado");
        } catch (Exception e) {
            log.error("Error inesperado actualizando estado driver {}: {}", driverId, e.getMessage(), e);
            sendResponse(driverWhatsapp, "⚠️ Error interno al actualizar tu estado.");
        }
    }

    // Actualizado para usar endpoint /short/{shortId}
    private UUID findRideIdFromShort(String shortId, String recipientNumberForError) {
        log.debug("Buscando UUID completo para ID corto (vía endpoint): {}", shortId);
        if (shortId == null || !shortId.matches("^[a-fA-F0-9]{8}$")) {
            log.error("ID corto inválido recibido: {}", shortId);
            sendResponse(recipientNumberForError, "El ID del viaje no es válido. Debe tener 8 caracteres (ej: 'Acepto 1a2b3c4d').");
            return null;
        }
        try {
            ResponseEntity<RideDto> response = rideServiceClient.findRideByShortId(shortId);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null && response.getBody().getId() != null) {
                UUID fullId = response.getBody().getId();
                log.info("UUID completo encontrado para {}: {}", shortId, fullId);
                return fullId;
            } else {
                log.warn("No se encontró viaje para ID corto {} (Respuesta: {})", shortId, response.getStatusCode());
                sendResponse(recipientNumberForError, "No se encontró el viaje con ID corto '" + shortId + "'. Verifica si lo escribiste bien o si ya finalizó/expiró.");
                return null;
            }
        } catch (FeignException e) {
            handleFeignException(e, recipientNumberForError, "buscar el viaje " + shortId);
            return null;
        } catch (Exception e) {
            log.error("Error inesperado buscando viaje por ID corto {}: {}", shortId, e.getMessage(), e);
            sendResponse(recipientNumberForError, "Error interno buscando el viaje.");
            return null;
        }
    }

    // Actualizado con respuesta
    private Optional<UUID> findActiveVehicleId(UUID driverId, String driverWhatsappForError) {
        log.debug("Buscando vehículo activo para conductor {}", driverId);
        try {
            ResponseEntity<DriverDto> response = driverServiceClient.getDriverDetailsById(driverId);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().getVehicles() != null) {
                Optional<VehicleDto> activeVehicle = response.getBody().getVehicles().stream()
                        .filter(v -> v != null && v.isActive())
                        .findFirst();
                if (activeVehicle.isPresent() && activeVehicle.get().getId() != null) {
                    UUID vehicleId = activeVehicle.get().getId();
                    log.info("Vehículo activo encontrado para conductor {}: {}", driverId, vehicleId);
                    return Optional.of(vehicleId);
                } else {
                    log.warn("Conductor {} no tiene vehículos activos registrados o válidos.", driverId);
                    sendResponse(driverWhatsappForError, "No tienes un vehículo activo asociado para aceptar servicios. Contacta a soporte si crees que es un error.");
                    return Optional.empty();
                }
            } else {
                log.warn("No se pudo obtener detalles de vehículos para conductor {}. Código: {}", driverId, response.getStatusCode());
                sendResponse(driverWhatsappForError, "No se pudo verificar tu vehículo activo. Intenta de nuevo más tarde.");
                return Optional.empty();
            }
        } catch (FeignException e) {
            handleFeignException(e, driverWhatsappForError, "buscar tu vehículo activo");
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error inesperado buscando vehículo activo para conductor {}: {}", driverId, e.getMessage(), e);
            sendResponse(driverWhatsappForError, "Error interno al buscar tu vehículo activo.");
            return Optional.empty();
        }
    }

    // Normalizador (sin cambios)
    private String normalizePhoneNumber(String rawNumber) {
        if (rawNumber == null) return null;
        String number = rawNumber.startsWith("whatsapp:") ? rawNumber.substring(9) : rawNumber;
        if (!number.startsWith("+")) return null;
        return number;
    }

    // Envío de respuesta (sin cambios)
    private void sendResponse(String toNumber, String message) {
        if (toNumber == null || message == null || message.isBlank()) {
            log.error("Intento de enviar respuesta inválida. To: {}, Message: '{}'", toNumber, message);
            return;
        }
        try {
            log.info("Enviando respuesta a {}: '{}'", toNumber, message);
            responseService.sendWhatsAppMessage(toNumber, message);
        } catch (Exception e) {
            log.error("Error al intentar enviar respuesta a {} (mensaje: '{}'): {}", toNumber, message, e.getMessage(), e);
        }
    }

    // --- Manejador de Errores Feign Centralizado ---
    private void handleFeignException(FeignException e, String recipientNumber, String actionDescription) {
        handleFeignException(e, recipientNumber, actionDescription, Map.of()); // Llama a la versión con mensajes custom
    }

    private void handleFeignException(FeignException e, String recipientNumber, String actionDescription, Map<HttpStatus, String> customMessages) {
        HttpStatus status = HttpStatus.resolve(e.status());
        String defaultErrorMsg = "Lo siento, hubo un error de comunicación con nuestros sistemas al intentar " + actionDescription + ". Intenta de nuevo más tarde.";
        String specificErrorMsg = null;

        if (status != null && customMessages.containsKey(status)) {
            specificErrorMsg = customMessages.get(status);
        } else if (status == HttpStatus.NOT_FOUND) {
            specificErrorMsg = "No se encontró la información necesaria para " + actionDescription + ".";
        } else if (status == HttpStatus.CONFLICT) {
            specificErrorMsg = "Hubo un conflicto al intentar " + actionDescription + ". Puede que la información ya exista o el estado no sea válido.";
        } else if (status != null && status.is5xxServerError()) {
            specificErrorMsg = "Nuestros sistemas internos tuvieron un problema al procesar tu solicitud para " + actionDescription + ". Ya estamos trabajando en ello.";
        }
        // Añadir más casos si es necesario (ej. 403 Forbidden)

        log.error("Error (Feign) al {}: Status={}, Body={}", actionDescription, e.status(), e.contentUTF8(), e);
        sendResponse(recipientNumber, specificErrorMsg != null ? specificErrorMsg : defaultErrorMsg);
    }

    // --- Extracción simple de origen (Placeholder) ---
    private String extractOriginFromMessage(String message) {
        // TODO: Implementar lógica más inteligente si es necesario (NLU)
        // Por ahora, si el mensaje contiene "desde", intenta tomar lo que sigue.
        int desdeIndex = message.indexOf("desde ");
        if (desdeIndex != -1) {
            return message.substring(desdeIndex + 6).trim(); // +6 para quitar "desde "
        }
        return null; // O devuelve un origen por defecto si no se especifica
    }

}