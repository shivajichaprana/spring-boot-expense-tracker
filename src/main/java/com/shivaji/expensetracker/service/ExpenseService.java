package com.shivaji.expensetracker.service;

import com.shivaji.expensetracker.dto.ExpenseRequest;
import com.shivaji.expensetracker.dto.ExpenseResponse;
import com.shivaji.expensetracker.exception.ResourceNotFoundException;
import com.shivaji.expensetracker.model.Category;
import com.shivaji.expensetracker.model.Expense;
import com.shivaji.expensetracker.model.User;
import com.shivaji.expensetracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Transactional
    @CacheEvict(value = "analytics", allEntries = true)
    public ExpenseResponse create(ExpenseRequest request, User user) {
        Expense expense = Expense.builder()
                .amount(request.getAmount())
                .category(request.getCategory())
                .description(request.getDescription())
                .date(request.getDate())
                .user(user)
                .build();

        expense = expenseRepository.save(expense);
        return ExpenseResponse.from(expense);
    }

    @Transactional(readOnly = true)
    public Page<ExpenseResponse> findAll(Long userId, Category category,
                                         LocalDate startDate, LocalDate endDate,
                                         Pageable pageable) {
        Page<Expense> expenses;

        if (category != null && startDate != null && endDate != null) {
            expenses = expenseRepository.findByUserIdAndCategoryAndDateBetween(
                    userId, category, startDate, endDate, pageable);
        } else if (category != null) {
            expenses = expenseRepository.findByUserIdAndCategory(userId, category, pageable);
        } else if (startDate != null && endDate != null) {
            expenses = expenseRepository.findByUserIdAndDateBetween(
                    userId, startDate, endDate, pageable);
        } else {
            expenses = expenseRepository.findByUserId(userId, pageable);
        }

        return expenses.map(ExpenseResponse::from);
    }

    @Transactional(readOnly = true)
    public ExpenseResponse findById(Long id, Long userId) {
        Expense expense = expenseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", id));

        return ExpenseResponse.from(expense);
    }

    @Transactional
    @CacheEvict(value = "analytics", allEntries = true)
    public ExpenseResponse update(Long id, ExpenseRequest request, Long userId) {
        Expense expense = expenseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", id));

        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setDescription(request.getDescription());
        expense.setDate(request.getDate());

        expense = expenseRepository.save(expense);
        return ExpenseResponse.from(expense);
    }

    @Transactional
    @CacheEvict(value = "analytics", allEntries = true)
    public void delete(Long id, Long userId) {
        Expense expense = expenseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", id));

        expenseRepository.delete(expense);
    }
}
