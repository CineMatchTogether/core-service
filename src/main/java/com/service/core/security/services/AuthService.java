package com.service.core.security.services;

import com.service.core.models.entities.RefreshToken;
import com.service.core.models.entities.Role;
import com.service.core.models.entities.User;
import com.service.core.models.entities.enums.ERole;
import com.service.core.repositories.RoleRepository;
import com.service.core.security.payload.LoginRequest;
import com.service.core.security.payload.SignupRequest;
import com.service.core.security.services.exception.EmptyTokenRefreshException;
import com.service.core.security.services.exception.TokenRefreshException;
import com.service.core.services.UserService;
import com.service.core.services.exceptions.UserAlreadyExistException;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@AllArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;

    private PasswordEncoder encoder;

    private final RefreshTokenService refreshTokenService;

    private final UserService userService;

    private final RoleRepository roleRepository;

    public UserDetailsImpl login(LoginRequest loginRequest) {

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return (UserDetailsImpl) authentication.getPrincipal();
    }

    public User refreshToken(String refreshToken) throws TokenRefreshException, EmptyTokenRefreshException {
        if (refreshToken == null || refreshToken.isEmpty())
            throw new EmptyTokenRefreshException("Refresh Token is empty!");
        RefreshToken token = refreshTokenService.findByToken(refreshToken).orElseThrow(() -> new TokenRefreshException(
                "Refresh token is not in database!"));

        return refreshTokenService.verifyExpiration(token).getUser();
    }

    public User registerUser(SignupRequest signUpRequest) throws UserAlreadyExistException {

        User user = User.builder()
                .username(signUpRequest.username())
                .password(encoder.encode(signUpRequest.password()))
                .build();

        Set<String> strRoles = signUpRequest.roles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);
                        break;

                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }
        user.setRoles(roles);

        return userService.createUser(user);
    }

}
