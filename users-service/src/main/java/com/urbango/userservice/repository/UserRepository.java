package com.urbango.userservice.repository;

import com.urbango.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository // Indica a Spring que esto es un bean de repositorio
public interface UserRepository  extends JpaRepository<User, UUID> {

    // Spring Data JPA generará automáticamente los métodos CRUD básicos:
    // save(User user), findById(UUID id), findAll(), deleteById(UUID id), etc.

    // Podemos definir métodos de consulta personalizados siguiendo convenciones de nombrado.
    // Por ejemplo, para buscar un usuario por su número de WhatsApp:
    Optional<User> findByWhatsappNumber(String whatsappNumber);

    // Spring Data JPA entenderá que quieres buscar en la entidad User
    // un campo llamado 'whatsappNumber' que coincida con el parámetro proporcionado.
    // Usamos Optional<User> porque el usuario podría no existir.

    // Podrías añadir otros métodos si los necesitas, por ejemplo:
    // boolean existsByWhatsapp

}
