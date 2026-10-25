package com.patelsamaj.patelsamaj_backend.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class ForgotPasswordDTO {
    @Email(message = "Please provide a valid email address")
    private String email;
}
