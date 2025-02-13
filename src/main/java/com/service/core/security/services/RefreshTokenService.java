package com.service.core.security.services;

import com.service.core.models.entities.RefreshToken;
import com.service.core.repositories.RefreshTokenRepository;
import com.service.core.repositories.UserRepository;
import com.service.core.security.services.exception.TokenRefreshException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    @Value("${property.app.jwtRefreshExpirationMs}")
    private Long refreshTokenDurationMs;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public RefreshToken createRefreshToken(UUID userId) {
        RefreshToken RefreshTokenModel = new RefreshToken();

        RefreshTokenModel.setUser(userRepository.findById(userId).get());
        RefreshTokenModel.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        RefreshTokenModel.setToken(UUID.randomUUID().toString());

        refreshTokenRepository.deleteByUser(userRepository.findById(userId).get());
        refreshTokenRepository.flush();
        RefreshTokenModel = refreshTokenRepository.save(RefreshTokenModel);
        return RefreshTokenModel;
    }

    public RefreshToken verifyExpiration(RefreshToken token) throws TokenRefreshException {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException("Refresh token was expired. Please make a new login request");
        }

        return token;
    }

    @Transactional
    public void deleteByUserId(UUID userId) {
        refreshTokenRepository.deleteByUser(userRepository.findById(userId).get());
    }
}
