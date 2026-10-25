package com.patelsamaj.patelsamaj_backend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ExpenseDTO {
    private Integer expenseId;

    @NotBlank(message = "Description cannot be blank")
    private String description;

    @NotNull(message = "Amount is required")
    @Positive(message = "Expense amount must be greater than zero")
    private Integer amount;

    @NotNull(message = "Expense date is required")
    private LocalDate expenseDate;

    private String category;
}
