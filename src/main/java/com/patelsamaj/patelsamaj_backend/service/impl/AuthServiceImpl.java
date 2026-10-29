package com.patelsamaj.patelsamaj_backend.service.impl;

import com.patelsamaj.patelsamaj_backend.config.JwtProperties;
import com.patelsamaj.patelsamaj_backend.dto.AuthResponse;
import com.patelsamaj.patelsamaj_backend.dto.AuthenticationDTO;
import com.patelsamaj.patelsamaj_backend.model.RefreshTokenEntity;
import com.patelsamaj.patelsamaj_backend.model.User;
import com.patelsamaj.patelsamaj_backend.repository.RefreshTokenRepository;
import com.patelsamaj.patelsamaj_backend.repository.UserRepository;
import com.patelsamaj.patelsamaj_backend.service.AuthService;
import com.patelsamaj.patelsamaj_backend.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder bPasswordEncoder;
    private final TokenService tokenService;
    private final JwtProperties jwtProperties;

    @Override
    @Transactional
    public Optional<AuthResponse> login(AuthenticationDTO dto) {
        String mobileNumber = dto.getMobileNumber() != null ? dto.getMobileNumber().trim() : "";
        User user = userRepository.findByMobileNumberAndIsActiveTrue(mobileNumber).orElse(null);
        if (user == null || !Boolean.TRUE.equals(user.getIsActive())) {
            return Optional.empty();
        }

        boolean isPasswordValid = bPasswordEncoder.matches(dto.getPassword(), user.getPassword());
        if (!isPasswordValid) {
            return Optional.empty();
        }

        String jwtToken = tokenService.generateJWTToken(user);
        String refreshToken = tokenService.generateRefreshToken();
        String hashedRefreshToken = tokenService.hashRefreshToken(refreshToken);

        RefreshTokenEntity entity = RefreshTokenEntity.builder()
                .user(user)
                .tokenHash(hashedRefreshToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(jwtProperties.getRefreshExpirationMs())))
                .build();
        refreshTokenRepository.save(entity);

        LocalDateTime expireAt = LocalDateTime.now()
                .plus(Duration.ofMillis(jwtProperties.getExpirationMs()));

        AuthResponse response = AuthResponse.builder()
                .jwtToken(jwtToken)
                .refreshToken(refreshToken)
                .expireAt(expireAt)
                .build();

        return Optional.of(response);
    }

    @Override
    @Transactional
    public Optional<AuthResponse> refreshToken(String refreshToken) {
        String hashRefreshToken = tokenService.hashRefreshToken(refreshToken.trim());

        RefreshTokenEntity tokenEntity = refreshTokenRepository.findByTokenHash(hashRefreshToken)
                .orElse(null);

        if (tokenEntity == null || !tokenEntity.isActive()) {
            return Optional.empty();
        }

        User user = tokenEntity.getUser();
        if (user == null || !Boolean.TRUE.equals(user.getIsActive())) {
            return Optional.empty();
        }

        String newRefreshToken = tokenService.generateRefreshToken();
        String newHashRefreshToken = tokenService.hashRefreshToken(newRefreshToken);

        RefreshTokenEntity newRefreshTokenEntity = RefreshTokenEntity.builder()
                .user(user)
                .tokenHash(newHashRefreshToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(tokenEntity.getExpiresAt())
                .build();

        newRefreshTokenEntity = refreshTokenRepository.save(newRefreshTokenEntity);

        tokenEntity.setRevokedAt(LocalDateTime.now());
        tokenEntity.setReplacedByTokenId(newRefreshTokenEntity.getId());
        refreshTokenRepository.save(tokenEntity);

        String jwtToken = tokenService.generateJWTToken(user);

        LocalDateTime expireAt = LocalDateTime.now()
                .plus(Duration.ofMillis(jwtProperties.getExpirationMs()));

        AuthResponse response = AuthResponse.builder()
                .jwtToken(jwtToken)
                .refreshToken(newRefreshToken)
                .expireAt(expireAt)
                .build();

        return Optional.of(response);
    }

    @Override
    @Transactional
    public boolean revokeToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return false;
        }

        String hashRefreshToken = tokenService.hashRefreshToken(refreshToken.trim());
        Optional<RefreshTokenEntity> tokenEntityOpt = refreshTokenRepository.findByTokenHash(hashRefreshToken);

        if (tokenEntityOpt.isEmpty()) {
            return false;
        }

        RefreshTokenEntity tokenEntity = tokenEntityOpt.get();
        if (tokenEntity.isRevoked()) {
            return false;
        }

        tokenEntity.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(tokenEntity);
        return true;
    }
}
