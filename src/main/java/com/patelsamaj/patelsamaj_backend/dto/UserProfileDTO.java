package com.patelsamaj.patelsamaj_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserProfileDTO {
    private Integer userId;

    @NotBlank(message = "Full name cannot be empty")
    private String fullName;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be exactly 10 digits")
    private String mobileNumber;

    @Email(message = "Please provide a valid email address")
    private String email;

    private Boolean isActive;

    private String roleName;
}

