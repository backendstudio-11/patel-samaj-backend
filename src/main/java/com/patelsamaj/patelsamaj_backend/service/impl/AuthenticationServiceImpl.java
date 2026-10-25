package com.patelsamaj.patelsamaj_backend.service.impl;

import com.patelsamaj.patelsamaj_backend.dto.AuthenticationDTO;
import com.patelsamaj.patelsamaj_backend.repository.UserRepository;
import com.patelsamaj.patelsamaj_backend.security.JwtService;
import com.patelsamaj.patelsamaj_backend.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository repository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public String authenticate(AuthenticationDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getMobileNumber(),
                        request.getPassword()
                )
        );

        var user = repository.findByMobileNumberAndIsActiveTrue(request.getMobileNumber())
                .orElseThrow();

        String jwtToken = jwtService.generateToken(user);

        return jwtToken;
    }
}