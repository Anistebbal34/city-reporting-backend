package com.anistebbal.starter.services;

import com.anistebbal.starter.dto.RegisterUserDto;
import com.anistebbal.starter.dto.UserResponseDto;
import com.anistebbal.starter.entities.Street;
import com.anistebbal.starter.entities.User;
import com.anistebbal.starter.repositories.StreetRepository;
import com.anistebbal.starter.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jakarta.persistence.EntityNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StreetRepository streetRepository;

    @Mock
    private AuthenticationManager authManager;

    @Mock
    private JWTService jwtService;

    @InjectMocks
    private UserService userService;
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerUser_success() {
        // Arrange
        RegisterUserDto dto = new RegisterUserDto();
        dto.setUsername("john");
        dto.setPhone("0555123456");
        dto.setPassword("password");
        dto.setRole("DRIVER");
        dto.setStreetId(1L);

        Street street = new Street();
        street.setId(1L);
        when(streetRepository.findById(1L)).thenReturn(Optional.of(street));

        User user = User.builder()
                .username(dto.getUsername())
                .phone(dto.getPhone())
                .password("encoded") // mock encoded
                .role(dto.getRole())
                .street(street)
                .build();

        User savedUser = User.builder()
                .id(100L)
                .username("john")
                .phone("0555123456")
                .role("DRIVER")
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        UserResponseDto response = userService.registerUser(dto);

        // Assert
        assertNotNull(response);
        assertEquals("john", response.getUsername());
        assertEquals("0555123456", response.getPhone());
        assertEquals("DRIVER", response.getRole());
    }

    @Test
    void registerUser_throwsIfStreetNotFound() {
        RegisterUserDto dto = new RegisterUserDto();
        dto.setStreetId(99L);
        when(streetRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> userService.registerUser(dto));

        assertTrue(ex.getMessage().contains("Street not found"));
    }

    @Test
    void updateUser_success_returnsUpdatedUser() {
        // Arrange
        Long userId = 1L;
        Long streetId = 2L;

        RegisterUserDto dto = new RegisterUserDto();
        dto.setUsername("updatedUser");
        dto.setPhone("0777123456");
        dto.setPassword("newpass");
        dto.setRole("ADMIN");
        dto.setStreetId(streetId);

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setUsername("oldUser");
        existingUser.setPhone("0555000000");
        existingUser.setPassword("oldpass");
        existingUser.setRole("USER");

        Street newStreet = new Street();
        newStreet.setId(streetId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(streetRepository.findById(streetId)).thenReturn(Optional.of(newStreet));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserResponseDto updatedUser = userService.updateUser(userId, dto);

        // Assert
        assertEquals("updatedUser", updatedUser.getUsername());
        assertEquals("0777123456", updatedUser.getPhone());
        assertEquals("ADMIN", updatedUser.getRole());

    }

}
