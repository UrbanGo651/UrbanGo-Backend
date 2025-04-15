package com.urbango.userservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Data // Genera getters, setters, toString, equals, hashCode
@NoArgsConstructor // Genera constructor sin argumentos
@AllArgsConstructor // Genera constructor con todos los argumentos
@Entity
@Table(name = "users") // Schema definido en application-dev.yml
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    // La BD genera el UUID con DEFAULT, no necesitamos @GeneratedValue explícito aquí.
    // Hibernate debería detectarlo. Si da problemas al guardar, prueba con @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "whatsapp_number", unique = true, nullable = false, length = 25)
    private String whatsappNumber;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false, length = 50)
    private String status = "ACTIVE"; // Valor por defecto

    @CreationTimestamp // Hibernate asigna automáticamente la fecha de creación
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp // Hibernate actualiza automáticamente en cada modificación
    @Column(name = "updated_at")
    private Instant updatedAt;

    // Podrías añadir un constructor específico si lo necesitas
    // public User(String whatsappNumber, String fullName) {
    //     this.whatsappNumber = whatsappNumber;
    //     this.fullName = fullName;
    //     // El ID será generado por la BD
    //     // El status, createdAt, updatedAt se manejan automáticamente o con valores por defecto
    // }
}