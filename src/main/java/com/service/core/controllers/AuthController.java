package com.service.core.controllers;

import com.service.core.mappers.UserMapper;
import com.service.core.models.dto.UserDto;
import com.service.core.models.entities.RefreshToken;
import com.service.core.models.entities.User;
import com.service.core.security.jwt.JwtUtils;
import com.service.core.security.requests.LoginRequest;
import com.service.core.security.requests.SignupRequest;
import com.service.core.security.services.AuthService;
import com.service.core.security.services.RefreshTokenService;
import com.service.core.security.services.UserDetailsImpl;
import com.service.core.security.services.exception.EmptyTokenRefreshException;
import com.service.core.security.services.exception.TokenRefreshException;
import com.service.core.services.exceptions.UserAlreadyExistException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {
    private final JwtUtils jwtUtils;

    private final RefreshTokenService refreshTokenService;

    private final AuthService authService;

    private final UserMapper userMapper;

    @Operation(summary = "Login user")
    @PostMapping("/login")
    public ResponseEntity<UserDto> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        UserDetailsImpl userDetails = authService.login(loginRequest);
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());
        ResponseCookie jwtRefreshCookie = jwtUtils.generateRefreshJwtCookie(refreshToken.getToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, jwtRefreshCookie.toString())
                .body(userMapper.toDto(userDetails));
    }

    @Operation(summary = "Registration user")
    @PostMapping("/signup")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody SignupRequest signUpRequest) throws UserAlreadyExistException {
        return ResponseEntity.ok().body(userMapper.toDto(authService.registerUser(signUpRequest)));
    }

    @Operation(summary = "Logout user")
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {

        ResponseCookie jwtCookie = jwtUtils.getCleanJwtCookie();
        ResponseCookie jwtRefreshCookie = jwtUtils.getCleanJwtRefreshCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, jwtRefreshCookie.toString())
                .body("You've been signed out!");
    }

    @Operation(summary = "Get access token by refresh token")
    @PostMapping("/refreshtoken")
    public ResponseEntity<UserDto> refreshtoken(HttpServletRequest request) throws TokenRefreshException, EmptyTokenRefreshException {
        String refreshToken = jwtUtils.getJwtRefreshFromCookies(request);
        User user = authService.refreshToken(refreshToken);
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(user);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(userMapper.toDto(user));

    }
}
