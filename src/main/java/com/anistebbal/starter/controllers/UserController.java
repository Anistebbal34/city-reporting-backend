package com.anistebbal.starter.controllers;

import com.anistebbal.starter.dto.LoginRequestDTO;
import com.anistebbal.starter.dto.LoginResponseDto;
import com.anistebbal.starter.dto.RegisterUserDto;
import com.anistebbal.starter.dto.UserResponseDto;
import com.anistebbal.starter.entities.Street;
import com.anistebbal.starter.entities.User;
import com.anistebbal.starter.repositories.StreetRepository;
import com.anistebbal.starter.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private StreetRepository streetRepository;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDTO user) {
        LoginResponseDto userresponse = userService.verify(user);
        return ResponseEntity.ok(userresponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register")

    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody RegisterUserDto dto) {
        UserResponseDto response = userService.registerUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @Valid @RequestBody RegisterUserDto dto) {
        User updatedUser = userService.updateUser(userId, dto); // 🔁 Pass the DTO directly
        return ResponseEntity.ok(updatedUser);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok().body("User deleted successfully");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping("/street/{streetId}")
    public ResponseEntity<List<User>> getUsersByStreet(@PathVariable Long streetId) {
        return ResponseEntity.ok(userService.getUsersByStreetId(streetId));
    }

    @GetMapping("/district/{districtId}")
    public ResponseEntity<List<User>> getUsersByDistrict(@PathVariable Long districtId) {
        return ResponseEntity.ok(userService.getUsersByDistrictId(districtId));
    }
}
