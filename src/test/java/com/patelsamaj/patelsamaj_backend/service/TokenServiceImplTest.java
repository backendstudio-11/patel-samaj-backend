package com.patelsamaj.patelsamaj_backend.service;

import com.patelsamaj.patelsamaj_backend.config.JwtProperties;
import com.patelsamaj.patelsamaj_backend.model.Role;
import com.patelsamaj.patelsamaj_backend.model.User;
import com.patelsamaj.patelsamaj_backend.service.impl.TokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceImplTest {

    private TokenServiceImpl tokenService;
    private JwtProperties jwtProperties;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret("c2ZqZWhmY2F3bmRhd2xlY2p3bGYzNDJuaHdsZHdsbXhrZWRld2huZGhmaXdlbGNmc25lamZkX3BhdGVsc2FtYWpfc2VjdXJlX2p3dF9rZXlfMjAyNg==");
        jwtProperties.setExpirationMs(3600000L);
        jwtProperties.setRefreshExpirationMs(604800000L);

        tokenService = new TokenServiceImpl(jwtProperties);
        tokenService.init();

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
    void testJwtTokenGenerationAndValidation() {
        String token = tokenService.generateJWTToken(testUser);

        assertNotNull(token);
        assertTrue(tokenService.isTokenValid(token, testUser));
        assertEquals(10, tokenService.extractUserId(token));

        User otherUser = new User();
        otherUser.setUserId(99);
        assertFalse(tokenService.isTokenValid(token, otherUser));
    }

    @Test
    void testIsTokenValid_InvalidToken() {
        assertFalse(tokenService.isTokenValid("invalid.jwt.token", testUser));
    }

    @Test
    void testGenerateRefreshToken() {
        String token1 = tokenService.generateRefreshToken();
        String token2 = tokenService.generateRefreshToken();

        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2);
    }

    @Test
    void testHashRefreshToken() {
        String rawToken = "sample-token-123";
        String hash1 = tokenService.hashRefreshToken(rawToken);
        String hash2 = tokenService.hashRefreshToken(rawToken);

        assertNotNull(hash1);
        assertEquals(64, hash1.length());
        assertEquals(hash1, hash2);
    }
}
