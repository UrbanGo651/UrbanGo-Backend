package com.urbango.notificationservice.client;

import com.urbango.notificationservice.dto.external.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

@FeignClient(name = "users-service", url = "${services.user.url}/api/v1/users") // Nombre del servicio en plural
public interface UserServiceClient {
    @GetMapping("/{id}")
    ResponseEntity<UserDto> getUserById(@PathVariable("id") UUID userId);
}
