package com.shivaji.expensetracker.dto;

import com.shivaji.expensetracker.model.Category;
import com.shivaji.expensetracker.model.Expense;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {

    private Long id;
    private BigDecimal amount;
    private Category category;
    private String description;
    private LocalDate date;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ExpenseResponse from(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .amount(expense.getAmount())
                .category(expense.getCategory())
                .description(expense.getDescription())
                .date(expense.getDate())
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .build();
    }
}
