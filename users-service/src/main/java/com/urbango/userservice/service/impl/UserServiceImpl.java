package com.urbango.userservice.service.impl;


import com.urbango.userservice.dto.CreateUserRequestDto;
import com.urbango.userservice.dto.UserDto;
import com.urbango.userservice.entity.User;
import com.urbango.userservice.repository.UserRepository;
import com.urbango.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service //Marca esta clase como un bean de servicio de Spring
@RequiredArgsConstructor // Lombok: genera constructor con argumentos para campos final (Inyección)
@Slf4j // Lombok: genera un logger SLF4J llamado log
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository; // Inyección de dependencia vía constructor


    @Override
    @Transactional // Asegura que la operación se ejecute dentro de una transacción
    public UserDto createUser(CreateUserRequestDto requestDto) {
        log.info("Intentando crear usuario con WhatsApp: {}", requestDto.getWhatsappNumber());

        // 1. Verificar si el número de WhatsApp ya existe (evita duplicados)
        userRepository.findByWhatsappNumber(requestDto.getWhatsappNumber()).ifPresent(existingUser -> {
            log.warn("Intento de crear usuario con WhatsApp existente: {}", requestDto.getWhatsappNumber());
            // Aquí deberías lanzar una excepción personalizada, por ejemplo:
            throw new IllegalArgumentException("El número de WhatsApp '" + requestDto.getWhatsappNumber() + "' ya está registrado.");
            // O manejarlo devolviendo el usuario existente si esa es la lógica deseada.
        });

        // 2. Mapear DTO a Entidad
        User newUser = new User();
        newUser.setWhatsappNumber(requestDto.getWhatsappNumber());
        newUser.setFullName(requestDto.getFullName());
        // El ID será generado por la BD (o Hibernate si se configura).
        // El status, createdAt, updatedAt son manejados por defecto o por Hibernate.

        // 3. Guardar en la BD
        User savedUser = userRepository.save(newUser);
        log.info("Usuario creado exitosamente con ID: {}", savedUser.getId());

        // 4. Mapear Entidad guardada a DTO de respuesta
        return mapUserToUserDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true) // Transacción de solo lectura, optimiza rendimiento
    public Optional<UserDto> findUserByWhatsappNumber(String whatsappNumber) {
        log.debug("Buscando usuario por WhatsApp: {}", whatsappNumber);
        return userRepository.findByWhatsappNumber(whatsappNumber)
                .map(this::mapUserToUserDto); // Mapea el User a UserDto si se encuentra
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> findUserById(UUID id) {
        log.debug("Buscando usuario por ID: {}", id);
        return userRepository.findById(id)
                .map(this::mapUserToUserDto); // Mapea el User a UserDto si se encuentra
    }

    // --- Métodos de Mapeo Auxiliares ---
    // (Podrían ir en una clase Mapper separada si prefieres - ej. usando MapStruct)
    private UserDto mapUserToUserDto(User user) {
        if (user == null) {
            return null;
        }
        return new UserDto(
                user.getId(),
                user.getWhatsappNumber(),
                user.getFullName(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
