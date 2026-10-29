package com.patelsamaj.patelsamaj_backend.service;

import com.patelsamaj.patelsamaj_backend.config.JwtProperties;
import com.patelsamaj.patelsamaj_backend.dto.AuthResponse;
import com.patelsamaj.patelsamaj_backend.dto.AuthenticationDTO;
import com.patelsamaj.patelsamaj_backend.model.RefreshTokenEntity;
import com.patelsamaj.patelsamaj_backend.model.Role;
import com.patelsamaj.patelsamaj_backend.model.User;
import com.patelsamaj.patelsamaj_backend.repository.RefreshTokenRepository;
import com.patelsamaj.patelsamaj_backend.repository.UserRepository;
import com.patelsamaj.patelsamaj_backend.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder bPasswordEncoder;

    @Mock
    private TokenService tokenService;

    @Spy
    private JwtProperties jwtProperties = new JwtProperties();

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        jwtProperties.setExpirationMs(3600000L);
        jwtProperties.setRefreshExpirationMs(604800000L);

        Role role = new Role();
        role.setRoleId(1);
        role.setRoleName("USER");

        testUser = new User();
        testUser.setUserId(10);
        testUser.setFullName("Test User");
        testUser.setMobileNumber("9876543210");
        testUser.setPassword("encodedPassword");
        testUser.setRole(role);
        testUser.setIsActive(true);
    }

    @Test
    void testLogin_Success() {
        AuthenticationDTO request = AuthenticationDTO.builder()
                .mobileNumber("9876543210")
                .password("plainPassword")
                .build();

        when(userRepository.findByMobileNumberAndIsActiveTrue("9876543210")).thenReturn(Optional.of(testUser));
        when(bPasswordEncoder.matches("plainPassword", "encodedPassword")).thenReturn(true);
        when(tokenService.generateJWTToken(testUser)).thenReturn("sample-jwt-token");
        when(tokenService.generateRefreshToken()).thenReturn("sample-refresh-token");
        when(tokenService.hashRefreshToken("sample-refresh-token")).thenReturn("HASHED-REFRESH-TOKEN");

        Optional<AuthResponse> responseOpt = authService.login(request);

        assertTrue(responseOpt.isPresent());
        AuthResponse response = responseOpt.get();
        assertEquals("sample-jwt-token", response.getJwtToken());
        assertEquals("sample-refresh-token", response.getRefreshToken());
        assertNotNull(response.getExpireAt());

        verify(refreshTokenRepository).save(any(RefreshTokenEntity.class));
    }

    @Test
    void testLogin_UserNotFound() {
        AuthenticationDTO request = AuthenticationDTO.builder()
                .mobileNumber("9876543210")
                .password("plainPassword")
                .build();

        when(userRepository.findByMobileNumberAndIsActiveTrue("9876543210")).thenReturn(Optional.empty());

        Optional<AuthResponse> responseOpt = authService.login(request);

        assertTrue(responseOpt.isEmpty());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void testLogin_BadPassword() {
        AuthenticationDTO request = AuthenticationDTO.builder()
                .mobileNumber("9876543210")
                .password("wrongPassword")
                .build();

        when(userRepository.findByMobileNumberAndIsActiveTrue("9876543210")).thenReturn(Optional.of(testUser));
        when(bPasswordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        Optional<AuthResponse> responseOpt = authService.login(request);

        assertTrue(responseOpt.isEmpty());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void testRefreshToken_Success() {
        String rawToken = "raw-refresh-token";
        String hashedToken = "HASHED-OLD";

        RefreshTokenEntity tokenEntity = RefreshTokenEntity.builder()
                .id(1)
                .user(testUser)
                .tokenHash(hashedToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        RefreshTokenEntity savedNewEntity = RefreshTokenEntity.builder()
                .id(2)
                .user(testUser)
                .tokenHash("HASHED-NEW")
                .createdAt(LocalDateTime.now())
                .expiresAt(tokenEntity.getExpiresAt())
                .build();

        when(tokenService.hashRefreshToken(rawToken)).thenReturn(hashedToken);
        when(tokenService.generateRefreshToken()).thenReturn("new-raw-refresh-token");
        when(tokenService.hashRefreshToken("new-raw-refresh-token")).thenReturn("HASHED-NEW");
        when(tokenService.generateJWTToken(testUser)).thenReturn("new-jwt-token");
        when(refreshTokenRepository.findByTokenHash(hashedToken)).thenReturn(Optional.of(tokenEntity));
        when(refreshTokenRepository.save(any(RefreshTokenEntity.class))).thenReturn(savedNewEntity);

        Optional<AuthResponse> responseOpt = authService.refreshToken(rawToken);

        assertTrue(responseOpt.isPresent());
        AuthResponse response = responseOpt.get();
        assertEquals("new-jwt-token", response.getJwtToken());
        assertEquals("new-raw-refresh-token", response.getRefreshToken());
        assertNotNull(response.getExpireAt());

        // Token rotation verified
        assertNotNull(tokenEntity.getRevokedAt());
        assertEquals(2, tokenEntity.getReplacedByTokenId());
        verify(refreshTokenRepository, times(2)).save(any(RefreshTokenEntity.class));
    }

    @Test
    void testRefreshToken_ExpiredOrRevoked() {
        String rawToken = "revoked-token";
        String hashedToken = "HASHED-REVOKED";

        RefreshTokenEntity revokedEntity = RefreshTokenEntity.builder()
                .id(1)
                .user(testUser)
                .tokenHash(hashedToken)
                .createdAt(LocalDateTime.now().minusDays(5))
                .expiresAt(LocalDateTime.now().plusDays(2))
                .revokedAt(LocalDateTime.now().minusDays(1)) // revoked!
                .build();

        when(tokenService.hashRefreshToken(rawToken)).thenReturn(hashedToken);
        when(refreshTokenRepository.findByTokenHash(hashedToken)).thenReturn(Optional.of(revokedEntity));

        Optional<AuthResponse> responseOpt = authService.refreshToken(rawToken);

        assertTrue(responseOpt.isEmpty());
    }

    @Test
    void testRevokeToken_Success() {
        String rawToken = "raw-token";
        String hashedToken = "HASHED-TOKEN";

        RefreshTokenEntity entity = RefreshTokenEntity.builder()
                .id(1)
                .tokenHash(hashedToken)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        when(tokenService.hashRefreshToken(rawToken)).thenReturn(hashedToken);
        when(refreshTokenRepository.findByTokenHash(hashedToken)).thenReturn(Optional.of(entity));

        boolean result = authService.revokeToken(rawToken);

        assertTrue(result);
        assertTrue(entity.isRevoked());
        verify(refreshTokenRepository).save(entity);
    }
}
