package com.urbango.ridesservice.client;

import com.urbango.ridesservice.dto.external.UserDto; // DTO externo
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

// Apunta a la URL de user-service
@FeignClient(name = "user-service", url = "${services.user.url:http://localhost:8081}/api/v1/users")
public interface UserServiceClient {

    @GetMapping("/{id}")
    ResponseEntity<UserDto> getUserById(@PathVariable("id") UUID userId);

}
