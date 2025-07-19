package com.anistebbal.starter.services;

import com.anistebbal.starter.dto.LoginRequestDTO;
import com.anistebbal.starter.dto.LoginResponseDto;
import com.anistebbal.starter.dto.RegisterUserDto;
import com.anistebbal.starter.dto.UserResponseDto;
import com.anistebbal.starter.entities.Street;
import com.anistebbal.starter.entities.User;
import com.anistebbal.starter.repositories.StreetRepository;
import com.anistebbal.starter.repositories.UserRepository;
// import org.springframework.security.authentication.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.core.Authentication;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JWTService jwtService;

    @Autowired
    AuthenticationManager authManager;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    @Autowired
    private StreetRepository streetRepository;

    private User buildUserFromDto(RegisterUserDto dto, Street street) {
        return User.builder()
                .username(dto.getUsername())
                .phone(dto.getPhone())
                .password(encoder.encode(dto.getPassword()))
                .role(dto.getRole())
                .street(street)
                .build();
    }

    private UserResponseDto mapToResponseDto(User user) {
        return new UserResponseDto(user.getId(), user.getUsername(), user.getPhone(), user.getRole());
    }

    private String buildDuplicateErrorMessage(DataIntegrityViolationException ex, RegisterUserDto dto) {
        String cause = ex.getRootCause() != null ? ex.getRootCause().getMessage() : "";
        if (cause.contains("user_phone")) {
            return "Phone number already taken: " + dto.getPhone();
        } else if (cause.contains("user_username")) {
            return "Username already taken: " + dto.getUsername();
        }
        return "Duplicate value";
    }

    // Add a new user
    public UserResponseDto registerUser(RegisterUserDto dto) {
        Street street = streetRepository.findById(dto.getStreetId())
                .orElseThrow(() -> new EntityNotFoundException("Street not found with ID: " + dto.getStreetId()));

        User user = buildUserFromDto(dto, street);

        try {
            User saved = userRepository.save(user);
            return mapToResponseDto(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException(buildDuplicateErrorMessage(ex, dto));
        }
    }

    // Update an existing user

    private void mapDtoToExistingUser(User user, RegisterUserDto dto, Street street) {
        user.setUsername(dto.getUsername());
        user.setPhone(dto.getPhone());
        user.setRole(dto.getRole());
        user.setStreet(street);

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(encoder.encode(dto.getPassword()));
        }
    }

    public UserResponseDto updateUser(Long userId, RegisterUserDto dto) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        Street street = streetRepository.findById(dto.getStreetId())
                .orElseThrow(() -> new EntityNotFoundException("Street not found with ID: " + dto.getStreetId()));

        mapDtoToExistingUser(existingUser, dto, street);

        try {
            User savedUser = userRepository.save(existingUser);

            return mapToResponseDto(savedUser);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException(buildDuplicateErrorMessage(ex, dto));
        }
    }

    // Delete a user
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found with ID: " + userId);
        }
        userRepository.deleteById(userId);
    }

    // Get users by street ID
    public List<UserResponseDto> getUsersByStreetId(Long streetId) {
        List<User> users = userRepository.findByStreetId(streetId);

        return users.stream()
                .map(user -> this.mapToResponseDto(user))
                .toList();
    }

    public List<UserResponseDto> getUsersByDistrictId(Long districtId) {
        List<User> users = userRepository.findByStreetDistrictId(districtId);

        return users.stream()
                .map(user -> this.mapToResponseDto(user))
                .toList();
    }

    public List<UserResponseDto> getAllUsers() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(user -> this.mapToResponseDto(user))
                .toList();
    }

    public UserResponseDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        return this.mapToResponseDto(user);
    }

    public LoginResponseDto verify(LoginRequestDTO user) {
        log.info("Attempting login for phone: {}", user.getPhone());

        try {
            User foundUser = userRepository.findByPhone(user.getPhone())
                    .orElseThrow(() -> new EntityNotFoundException("User not found with phone: " + user.getPhone()));

            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getPhone(), user.getPassword()));

            if (authentication.isAuthenticated()) {
                String token = jwtService.generateToken(foundUser);
                return new LoginResponseDto(token, foundUser.getRole(), foundUser.getUsername(), foundUser.getPhone());
            } else {
                throw new IllegalArgumentException("Invalid credentials");
            }

        } catch (EntityNotFoundException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid credentials");
        }
    }

}
