package com.patelsamaj.patelsamaj_backend.controller;

import com.patelsamaj.patelsamaj_backend.config.JwtProperties;
import com.patelsamaj.patelsamaj_backend.dto.AuthResponse;
import com.patelsamaj.patelsamaj_backend.dto.AuthenticationDTO;
import com.patelsamaj.patelsamaj_backend.exception.GlobalExceptionHandler;
import com.patelsamaj.patelsamaj_backend.service.AuthService;
import com.patelsamaj.patelsamaj_backend.service.UserService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @Mock
    private UserService userService;

    @Spy
    private JwtProperties jwtProperties = new JwtProperties();

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testLogin_SetsRefreshTokenCookieAndReturnsAuthResponse() throws Exception {
        AuthResponse authResponse = AuthResponse.builder()
                .jwtToken("access-token-123")
                .refreshToken("refresh-token-abc")
                .expireAt(LocalDateTime.now().plusHours(1))
                .build();

        when(authService.login(any(AuthenticationDTO.class))).thenReturn(Optional.of(authResponse));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mobileNumber\":\"9876543210\",\"password\":\"pass123\"}"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(cookie().value("refreshToken", "refresh-token-abc"))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(jsonPath("$.jwtToken").value("access-token-123"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-abc"))
                .andExpect(jsonPath("$.expireAt").exists());
    }

    @Test
    void testRefreshToken_WithoutBody_FromCookie_Success() throws Exception {
        AuthResponse authResponse = AuthResponse.builder()
                .jwtToken("new-access-token-456")
                .refreshToken("new-refresh-token-def")
                .expireAt(LocalDateTime.now().plusHours(1))
                .build();

        when(authService.refreshToken("sample-cookie-token")).thenReturn(Optional.of(authResponse));

        mockMvc.perform(post("/api/auth/refresh-token")
                        .cookie(new Cookie("refreshToken", "sample-cookie-token")))
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(cookie().value("refreshToken", "new-refresh-token-def"))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(jsonPath("$.jwtToken").value("new-access-token-456"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token-def"))
                .andExpect(jsonPath("$.expireAt").exists());
    }

    @Test
    void testRefreshToken_MissingCookie_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/refresh-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Refresh token is missing from cookie."));
    }

    @Test
    void testLogout_ClearsRefreshTokenCookie() throws Exception {
        when(authService.revokeToken("sample-cookie-token")).thenReturn(true);

        mockMvc.perform(post("/api/auth/logout")
                        .cookie(new Cookie("refreshToken", "sample-cookie-token")))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("refreshToken", 0));

        verify(authService).revokeToken("sample-cookie-token");
    }
}
