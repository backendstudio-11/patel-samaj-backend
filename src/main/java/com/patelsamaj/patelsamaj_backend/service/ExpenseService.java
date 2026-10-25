package com.patelsamaj.patelsamaj_backend.service;

import com.patelsamaj.patelsamaj_backend.dto.DailyExpenseSummaryDTO;
import com.patelsamaj.patelsamaj_backend.dto.ExpenseDTO;
import com.patelsamaj.patelsamaj_backend.dto.PaginationDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ExpenseService {
    ExpenseDTO getExpenseById(int expenseId);
    Page<ExpenseDTO> getAllExpense(PaginationDTO request);
    ExpenseDTO upsertExpense(ExpenseDTO dto, int userId);
    List<DailyExpenseSummaryDTO> getDailyUsage();
    void deleteExpenseById(int expenseId);
}
