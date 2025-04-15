package com.urbango.userservice.service;

import com.urbango.userservice.dto.CreateUserRequestDto;
import com.urbango.userservice.dto.UserDto;

import java.util.Optional;
import java.util.UUID;

public interface UserService {

    /**
     * crear un nuevo usuario
     *
     * @param requestDto DTO con la información del usuario a crear
     * @return El DTO del usuario creado
     * @throws // Manejo excepciones especificas(ej: WhatsappNumberAlreadyExistsException)
     */
    UserDto createUser(CreateUserRequestDto requestDto);

    /**
     * Busca un usuario por su número de WhatsApp.
     * @param whatsappNumber El número de WhatsApp a buscar.
     * @return Un Optional conteniendo el UserDto si se encuentra, o vacío si no.
     */
    Optional<UserDto> findUserByWhatsappNumber(String whatsappNumber);

    /**
     * Busca un usuario por su ID.
     * @param id El UUID del usuario a buscar.
     * @return Un Optional conteniendo el UserDto si se encuentra, o vacío si no.
     */
    Optional<UserDto> findUserById(UUID id);

    // Aquí podemos añadir métodos para actualizar, eliminar, etc. en el futuro.
}
