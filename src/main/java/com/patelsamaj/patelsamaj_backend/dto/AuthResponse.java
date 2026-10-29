package com.patelsamaj.patelsamaj_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String jwtToken;
    private String refreshToken;
    private LocalDateTime expireAt;
}
