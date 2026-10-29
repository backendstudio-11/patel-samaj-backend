package com.patelsamaj.patelsamaj_backend.service;

import com.patelsamaj.patelsamaj_backend.model.User;

public interface TokenService {

    Integer extractUserId(String token);

    String generateJWTToken(User user);

    boolean isTokenValid(String token, User user);

    String generateRefreshToken();

    String hashRefreshToken(String refreshToken);
}
