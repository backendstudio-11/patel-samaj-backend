package com.patelsamaj.patelsamaj_backend.service;

import com.patelsamaj.patelsamaj_backend.dto.AuthenticationDTO;

public interface AuthenticationService {
    String authenticate(AuthenticationDTO request);
}
