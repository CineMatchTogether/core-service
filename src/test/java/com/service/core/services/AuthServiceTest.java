package com.service.core.services;

import com.service.core.models.entities.RefreshToken;
import com.service.core.models.entities.Role;
import com.service.core.models.entities.User;
import com.service.core.models.entities.enums.ERole;
import com.service.core.repositories.RoleRepository;
import com.service.core.security.requests.LoginRequest;
import com.service.core.security.requests.SignupRequest;
import com.service.core.security.services.AuthService;
import com.service.core.security.services.RefreshTokenService;
import com.service.core.security.services.UserDetailsImpl;
import com.service.core.security.services.exception.EmptyTokenRefreshException;
import com.service.core.security.services.exception.TokenRefreshException;
import com.service.core.services.exceptions.UserAlreadyExistException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private UserService userService;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private AuthService authService;

    private LoginRequest loginRequest;
    private SignupRequest signupRequest;
    private UserDetailsImpl userDetails;
    private User user;
    private Role userRole;
    private Authentication authentication;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();
        loginRequest = new LoginRequest("testuser", "password123");
        signupRequest = new SignupRequest("testuser", "test@example.com", "password123", Set.of("ROLE_USER"));
        
        userRole = new Role();
        userRole.setName(ERole.ROLE_USER);
        
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        user = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .roles(roles)
                .build();

        userDetails = new UserDetailsImpl(
                userId,
                "testuser",
                "test@example.com",
                "encodedPassword",
                Set.of()
        );

        authentication = mock(Authentication.class);
        
        refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token");
        refreshToken.setUser(user);
    }

    @Test
    void login_Success() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // Act
        UserDetailsImpl result = authService.login(loginRequest);

        // Assert
        assertNotNull(result);
        assertEquals(userDetails.getUsername(), result.getUsername());
        assertEquals(userDetails.getEmail(), result.getEmail());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void refreshToken_Success() throws TokenRefreshException, EmptyTokenRefreshException {
        // Arrange
        String token = "valid-refresh-token";
        when(refreshTokenService.findByToken(token)).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.verifyExpiration(refreshToken)).thenReturn(refreshToken);

        // Act
        User result = authService.refreshToken(token);

        // Assert
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getUsername(), result.getUsername());
        verify(refreshTokenService).findByToken(token);
        verify(refreshTokenService).verifyExpiration(refreshToken);
    }

    @Test
    void refreshToken_EmptyToken() throws TokenRefreshException {
        // Arrange
        String token = "";

        // Act & Assert
        assertThrows(EmptyTokenRefreshException.class, () -> authService.refreshToken(token));
        verify(refreshTokenService, never()).findByToken(anyString());
        verify(refreshTokenService, never()).verifyExpiration(any());
    }

    @Test
    void refreshToken_TokenNotFound() throws TokenRefreshException {
        // Arrange
        String token = "invalid-token";
        when(refreshTokenService.findByToken(token)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TokenRefreshException.class, () -> authService.refreshToken(token));
        verify(refreshTokenService).findByToken(token);
        verify(refreshTokenService, never()).verifyExpiration(any());
    }

    @Test
    void registerUser_Success() throws UserAlreadyExistException {
        // Arrange
        when(passwordEncoder.encode(signupRequest.password())).thenReturn("encodedPassword");
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(userService.createUser(any(User.class))).thenReturn(user);

        // Act
        User result = authService.registerUser(signupRequest);

        // Assert
        assertNotNull(result);
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getEmail(), result.getEmail());
        assertTrue(result.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_USER));
        
        verify(passwordEncoder).encode(signupRequest.password());
        verify(roleRepository).findByName(ERole.ROLE_USER);
        verify(userService).createUser(any(User.class));
    }

    @Test
    void registerUser_WithAdminRole() throws UserAlreadyExistException {
        // Arrange
        Role adminRole = new Role();
        adminRole.setName(ERole.ROLE_ADMIN);
        
        SignupRequest adminSignupRequest = new SignupRequest(
                "admin",
                "admin@example.com",
                "password123",
                Set.of("ROLE_ADMIN")
        );

        when(passwordEncoder.encode(adminSignupRequest.password())).thenReturn("encodedPassword");
        when(roleRepository.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(userService.createUser(any(User.class))).thenReturn(user);

        // Act
        User result = authService.registerUser(adminSignupRequest);

        // Assert
        assertNotNull(result);
        verify(roleRepository).findByName(ERole.ROLE_ADMIN);
        verify(userService).createUser(any(User.class));
    }

    @Test
    void registerUser_RoleNotFound() throws UserAlreadyExistException {
        // Arrange
        when(passwordEncoder.encode(signupRequest.password())).thenReturn("encodedPassword");
        when(roleRepository.findByName(any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authService.registerUser(signupRequest));
        verify(passwordEncoder).encode(signupRequest.password());
        verify(roleRepository).findByName(any());
        verify(userService, never()).createUser(any());
    }
} 