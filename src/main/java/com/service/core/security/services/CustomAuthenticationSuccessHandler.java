package com.service.core.security.services;

import com.service.core.models.entities.RefreshToken;
import com.service.core.models.entities.User;
import com.service.core.security.jwt.JwtUtils;
import com.service.core.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler {
    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;

    @Value("${property.app.staticUrl}")
    private String staticUrl;

    @SneakyThrows
    public void oauthSuccessResponse(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        DefaultOAuth2User principal = (DefaultOAuth2User) authentication.getPrincipal();
        String username = principal.getAttribute("login");
        String email = principal.getAttribute("email");

        //find or create user
        User user = userService.creatOrGetOauthUser(username, email, principal);
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);

        //generate tokens
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());
        ResponseCookie jwtRefreshCookie = jwtUtils.generateRefreshJwtCookie(refreshToken.getToken());

        response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, jwtRefreshCookie.toString());

        //remove session
        request.getSession().invalidate();

        //redirect to frontend
        response.sendRedirect(staticUrl);
    }
}
