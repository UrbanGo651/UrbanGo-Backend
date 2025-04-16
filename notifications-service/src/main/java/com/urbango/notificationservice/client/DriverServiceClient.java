package com.urbango.notificationservice.client;
import com.urbango.notificationservice.dto.external.DriverDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

@FeignClient(name = "drivers-service", url = "${services.driver.url}/api/v1/drivers") // Nombre del servicio en plural
public interface DriverServiceClient {
    // Usamos /details porque asumimos que tiene el whatsappNumber
    @GetMapping("/{id}/details")
    ResponseEntity<DriverDto> getDriverDetailsById(@PathVariable("id") UUID driverId);
}