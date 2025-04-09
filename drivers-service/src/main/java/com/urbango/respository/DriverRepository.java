package com.urbango.respository;

import com.urbango.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    List<Driver> findByVehicleTypeAndStatus(String vehicleType, String status);
    Driver findByPhone(String phone);
}
