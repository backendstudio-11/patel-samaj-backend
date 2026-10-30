package com.patelsamaj.patelsamaj_backend.dto;

import java.time.LocalDate;

public interface DailyExpenseSummaryDTO {
    LocalDate getDate();
    Long getTotalSpent();
}
