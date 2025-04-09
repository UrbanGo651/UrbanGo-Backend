package com.urbango.respository;

import com.urbango.model.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RideRepository extends JpaRepository<Ride, Long> {
    // Aquí se agrega consultas personalizadas si es necesario
}