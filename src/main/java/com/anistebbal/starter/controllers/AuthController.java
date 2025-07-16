package com.anistebbal.starter.controllers;

import com.anistebbal.starter.dto.LoginRequestDTO;
import com.anistebbal.starter.dto.LoginResponseDto;
import com.anistebbal.starter.dto.RegisterUserDto;
import com.anistebbal.starter.dto.UserResponseDto;
import com.anistebbal.starter.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDTO loginDto) {
        LoginResponseDto response = userService.verify(loginDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody RegisterUserDto dto) {
        UserResponseDto response = userService.registerUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
