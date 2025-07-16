package com.anistebbal.starter.controllers;

import com.anistebbal.starter.dto.RegisterUserDto;
import com.anistebbal.starter.dto.UserResponseDto;

import com.anistebbal.starter.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody RegisterUserDto dto) {
        UserResponseDto updated = userService.updateUser(userId, dto);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(params = "streetId")
    public ResponseEntity<List<UserResponseDto>> getUsersByStreet(@RequestParam Long streetId) {
        return ResponseEntity.ok(userService.getUsersByStreetId(streetId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(params = "districtId")
    public ResponseEntity<List<UserResponseDto>> getUsersByDistrict(@RequestParam Long districtId) {
        return ResponseEntity.ok(userService.getUsersByDistrictId(districtId));
    }
}
