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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
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

    // Add a new user
    public UserResponseDto registerUser(RegisterUserDto dto) {
        Street street = streetRepository.findById(dto.getStreetId())
                .orElseThrow(() -> new EntityNotFoundException("Street not found with ID: " + dto.getStreetId()));

        User user = User.builder()
                .username(dto.getUsername())
                .phone(dto.getPhone())
                .password(encoder.encode(dto.getPassword()))
                .role(dto.getRole())
                .street(street)
                .build();

        try {
            User saved = userRepository.save(user);
            return new UserResponseDto(saved.getId(), saved.getUsername(), saved.getPhone(), saved.getRole());
        } catch (DataIntegrityViolationException ex) {
            String message = "Duplicate value";
            String cause = ex.getRootCause() != null ? ex.getRootCause().getMessage() : "";
            if (cause.contains("user_phone")) {
                message = "Phone number already taken: " + dto.getPhone();
            } else if (cause.contains("user_username")) {
                message = "Username already taken: " + dto.getUsername();
            }
            throw new IllegalArgumentException(message);
        }
    }

    // Update an existing user

    public User updateUser(Long userId, RegisterUserDto dto) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        Street street = streetRepository.findById(dto.getStreetId())
                .orElseThrow(() -> new EntityNotFoundException("Street not found with ID: " + dto.getStreetId()));

        existingUser.setUsername(dto.getUsername());
        existingUser.setPhone(dto.getPhone());
        existingUser.setRole(dto.getRole());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            existingUser.setPassword(encoder.encode(dto.getPassword()));
        }

        existingUser.setStreet(street);

        try {
            return userRepository.save(existingUser);
        } catch (DataIntegrityViolationException ex) {
            String cause = ex.getRootCause() != null ? ex.getRootCause().getMessage() : "";
            String message = "Duplicate value";

            if (cause.contains("user_phone")) {
                message = "Phone number already exists: " + dto.getPhone();
            } else if (cause.contains("user_username")) {
                message = "Username already exists: " + dto.getUsername();
            }

            throw new IllegalArgumentException(message);
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
    public List<User> getUsersByStreetId(Long streetId) {
        return userRepository.findByStreetId(streetId);
    }

    // Get users by district ID (via street.district)
    public List<User> getUsersByDistrictId(Long districtId) {
        return userRepository.findByStreetDistrictId(districtId);
    }

    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Optional: Get a specific user
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
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
