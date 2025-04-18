package com.urbango.whatsappadapter.client;

import com.urbango.whatsappadapter.client.dto.UserDto; // DTO externo
import com.urbango.whatsappadapter.dto.request.CreateUserRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "users-service", url = "${services.user.url}/api/v1/users")
public interface  UserServiceClient {
    // Para identificar remitente
    @GetMapping("/whatsapp/{whatsappNumber}")
    ResponseEntity<UserDto> findUserByWhatsappNumber(@PathVariable("whatsappNumber") String whatsappNumber);

    // Para registrar nuevo usuario
    @PostMapping
    ResponseEntity<UserDto> createUser(@RequestBody CreateUserRequestDto requestDto); // Necesitamos este DTO aquí o en common
}
