package com.patelsamaj.patelsamaj_backend.service;

import com.patelsamaj.patelsamaj_backend.dto.AuthResponse;
import com.patelsamaj.patelsamaj_backend.dto.AuthenticationDTO;

import java.util.Optional;

public interface AuthService {

    Optional<AuthResponse> login(AuthenticationDTO dto);

    Optional<AuthResponse> refreshToken(String refreshToken);

    boolean revokeToken(String refreshToken);
}
