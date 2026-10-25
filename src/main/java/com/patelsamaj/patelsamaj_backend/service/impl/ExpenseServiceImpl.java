package com.patelsamaj.patelsamaj_backend.service.impl;

import com.patelsamaj.patelsamaj_backend.dto.DailyExpenseSummaryDTO;
import com.patelsamaj.patelsamaj_backend.dto.ExpenseDTO;
import com.patelsamaj.patelsamaj_backend.dto.PaginationDTO;
import com.patelsamaj.patelsamaj_backend.exception.ResourceNotFoundException;
import com.patelsamaj.patelsamaj_backend.mapper.ExpenseMapper;
import com.patelsamaj.patelsamaj_backend.model.Expense;
import com.patelsamaj.patelsamaj_backend.repository.ExpenseRepository;
import com.patelsamaj.patelsamaj_backend.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;

    @Override
    public ExpenseDTO getExpenseById(int expenseId){
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));;
        return expenseMapper.toDto(expense);
    }

    @Override
    public Page<ExpenseDTO> getAllExpense(PaginationDTO request){
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(request.getDirection(), request.getSortBy())
        );
        Page<Expense> expensePage = expenseRepository.searchExpenses(request.getSearch(), pageable);
        return expensePage.map(expenseMapper::toDto);
    }

    @Override
    @Transactional
    public ExpenseDTO upsertExpense(ExpenseDTO dto, int userId) {
        Expense expense;
        if(dto.getExpenseId() != null && dto.getExpenseId() != 0){
            expense = expenseRepository.findById(dto.getExpenseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        }
        else{
            expense = new Expense();
            expense.setCreatedBy(userId);
        }
        expenseMapper.updateEntityFromDto(dto, expense);
        expenseRepository.save(expense);
        return expenseMapper.toDto(expense);
    }

    @Override
    public List<DailyExpenseSummaryDTO> getDailyUsage() {
        return expenseRepository.getDailyUsageSummary();
    }

    @Override
    @Transactional
    public void deleteExpenseById(int expenseId) {
        if (!expenseRepository.existsById(expenseId)) {
            throw new ResourceNotFoundException("Expense not found");
        }
        expenseRepository.deleteById(expenseId);
    }
}
