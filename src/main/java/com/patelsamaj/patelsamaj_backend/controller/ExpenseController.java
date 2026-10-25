package com.patelsamaj.patelsamaj_backend.controller;

import com.patelsamaj.patelsamaj_backend.dto.DailyExpenseSummaryDTO;
import com.patelsamaj.patelsamaj_backend.dto.ExpenseDTO;
import com.patelsamaj.patelsamaj_backend.dto.PaginationDTO;
import com.patelsamaj.patelsamaj_backend.helper.SecurityUtils;
import com.patelsamaj.patelsamaj_backend.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping("/list")
    public ResponseEntity<Page<ExpenseDTO>> getAllExpense(@RequestBody PaginationDTO request) {
        request.setSortBy("expenseDate");
        return ResponseEntity.ok(expenseService.getAllExpense(request));
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<ExpenseDTO> getExpenseById(@PathVariable int expenseId) {
        return ResponseEntity.ok(expenseService.getExpenseById(expenseId));
    }

    @PostMapping
    public ResponseEntity<ExpenseDTO> upsertExpense(@Valid @RequestBody ExpenseDTO dto) {
        int adminId = SecurityUtils.getAuthenticatedUserId();
        return ResponseEntity.ok(expenseService.upsertExpense(dto, adminId));
    }
    @GetMapping("/daily-summary")
    public ResponseEntity<List<DailyExpenseSummaryDTO>> getDailySummary() {
        return ResponseEntity.ok(expenseService.getDailyUsage());
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<String> deleteExpenseById(@PathVariable int expenseId) {
        expenseService.deleteExpenseById(expenseId);
        return ResponseEntity.ok("Expense deleted successfully.");
    }
}
