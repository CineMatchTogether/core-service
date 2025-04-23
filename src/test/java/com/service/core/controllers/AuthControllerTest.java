package com.service.core.controllers;

import com.service.core.mappers.UserMapper;
import com.service.core.models.dto.UserDto;
import com.service.core.models.entities.RefreshToken;
import com.service.core.models.entities.Role;
import com.service.core.models.entities.User;
import com.service.core.models.entities.enums.ERole;
import com.service.core.security.jwt.JwtUtils;
import com.service.core.security.requests.LoginRequest;
import com.service.core.security.requests.SignupRequest;
import com.service.core.security.services.AuthService;
import com.service.core.security.services.RefreshTokenService;
import com.service.core.security.services.UserDetailsImpl;
import com.service.core.security.services.exception.EmptyTokenRefreshException;
import com.service.core.security.services.exception.TokenRefreshException;
import com.service.core.services.exceptions.UserAlreadyExistException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AuthControllerTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AuthService authService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthController authController;

    private UserDetailsImpl userDetails;
    private User user;
    private UserDto userDto;
    private ResponseCookie jwtCookie;
    private ResponseCookie refreshCookie;

    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();

        // Setup roles
        Set<Role> roles = new HashSet<>();
        Role userRole = new Role();
        userRole.setName(ERole.ROLE_USER);
        roles.add(userRole);

        // Setup UserDetailsImpl
        userDetails = new UserDetailsImpl(
                userId,
                "testuser",
                "test@example.com",
                "password123",
                Set.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // Setup User entity
        user = new User();
        user.setId(userId);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setRoles(roles);

        // Setup UserDto
        userDto = new UserDto(
                userId,
                "testuser",
                "test@example.com",
                Set.of("ROLE_USER")
        );

        jwtCookie = ResponseCookie.from("jwt", "jwt-token").build();
        refreshCookie = ResponseCookie.from("refresh", "refresh-token").build();
    }

    @Test
    void authenticateUser_Success() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("testuser", "password123");
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token");

        when(authService.login(loginRequest)).thenReturn(userDetails);
        when(jwtUtils.generateJwtCookie(userDetails)).thenReturn(jwtCookie);
        when(refreshTokenService.createRefreshToken(userDetails.getId())).thenReturn(refreshToken);
        when(jwtUtils.generateRefreshJwtCookie(refreshToken.getToken())).thenReturn(refreshCookie);
        when(userMapper.toDto(userDetails)).thenReturn(userDto);

        // Act
        ResponseEntity<UserDto> response = authController.authenticateUser(loginRequest);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(userDto, response.getBody());
        assertTrue(response.getHeaders().get(HttpHeaders.SET_COOKIE).contains(jwtCookie.toString()));
        assertTrue(response.getHeaders().get(HttpHeaders.SET_COOKIE).contains(refreshCookie.toString()));

        verify(authService).login(loginRequest);
        verify(jwtUtils).generateJwtCookie(userDetails);
        verify(refreshTokenService).createRefreshToken(userDetails.getId());
        verify(jwtUtils).generateRefreshJwtCookie(refreshToken.getToken());
        verify(userMapper).toDto(userDetails);
    }

    @Test
    void registerUser_Success() throws UserAlreadyExistException {
        // Arrange
        SignupRequest signupRequest = new SignupRequest(
                "testuser",
                "test@example.com",
                "password123",
                Set.of("ROLE_USER")
        );

        when(authService.registerUser(signupRequest)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userDto);

        // Act
        ResponseEntity<UserDto> response = authController.registerUser(signupRequest);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(userDto, response.getBody());

        verify(authService).registerUser(signupRequest);
        verify(userMapper).toDto(user);
    }

    @Test
    void registerUser_UserAlreadyExists() throws UserAlreadyExistException {
        // Arrange
        SignupRequest signupRequest = new SignupRequest(
                "testuser",
                "test@example.com",
                "password123",
                Set.of("ROLE_USER")
        );

        when(authService.registerUser(signupRequest)).thenThrow(new UserAlreadyExistException("User exists"));

        // Act & Assert
        assertThrows(UserAlreadyExistException.class, () -> authController.registerUser(signupRequest));
        verify(authService).registerUser(signupRequest);
        verify(userMapper, never()).toDto(any(UserDetailsImpl.class));
    }

    @Test
    void logoutUser_Success() {
        // Arrange
        when(jwtUtils.getCleanJwtCookie()).thenReturn(jwtCookie);
        when(jwtUtils.getCleanJwtRefreshCookie()).thenReturn(refreshCookie);

        // Act
        ResponseEntity<?> response = authController.logoutUser();

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals("You've been signed out!", response.getBody());
        assertTrue(response.getHeaders().get(HttpHeaders.SET_COOKIE).contains(jwtCookie.toString()));
        assertTrue(response.getHeaders().get(HttpHeaders.SET_COOKIE).contains(refreshCookie.toString()));

        verify(jwtUtils).getCleanJwtCookie();
        verify(jwtUtils).getCleanJwtRefreshCookie();
    }

    @Test
    void refreshtoken_Success() throws TokenRefreshException, EmptyTokenRefreshException {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        String refreshToken = "refresh-token";

        when(jwtUtils.getJwtRefreshFromCookies(request)).thenReturn(refreshToken);
        when(authService.refreshToken(refreshToken)).thenReturn(user);
        when(jwtUtils.generateJwtCookie(user)).thenReturn(jwtCookie);
        when(userMapper.toDto(user)).thenReturn(userDto);

        // Act
        ResponseEntity<UserDto> response = authController.refreshtoken(request);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(userDto, response.getBody());
        assertTrue(response.getHeaders().get(HttpHeaders.SET_COOKIE).contains(jwtCookie.toString()));

        verify(jwtUtils).getJwtRefreshFromCookies(request);
        verify(authService).refreshToken(refreshToken);
        verify(jwtUtils).generateJwtCookie(user);
        verify(userMapper).toDto(user);
    }
}


