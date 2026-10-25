package com.patelsamaj.patelsamaj_backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DonorDTO {
    private Integer donorId;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotNull(message = "Donation amount is required")
    @Min(value = 1, message = "Amount must be greater than zero")
    private Integer amount;

    private LocalDateTime donationDate;

    private String remarks;
}
