package com.shivaji.expensetracker.service;

import com.shivaji.expensetracker.dto.ExpenseRequest;
import com.shivaji.expensetracker.dto.ExpenseResponse;
import com.shivaji.expensetracker.exception.ResourceNotFoundException;
import com.shivaji.expensetracker.model.Category;
import com.shivaji.expensetracker.model.Expense;
import com.shivaji.expensetracker.model.User;
import com.shivaji.expensetracker.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private ExpenseService expenseService;

    private User testUser;
    private Expense testExpense;
    private ExpenseRequest testRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .build();

        testExpense = Expense.builder()
                .id(1L)
                .amount(new BigDecimal("25.50"))
                .category(Category.FOOD)
                .description("Lunch")
                .date(LocalDate.of(2026, 3, 15))
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testRequest = ExpenseRequest.builder()
                .amount(new BigDecimal("25.50"))
                .category(Category.FOOD)
                .description("Lunch")
                .date(LocalDate.of(2026, 3, 15))
                .build();
    }

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("should create expense and return response")
        void shouldCreateExpense() {
            when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);

            ExpenseResponse response = expenseService.create(testRequest, testUser);

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getAmount()).isEqualByComparingTo("25.50");
            assertThat(response.getCategory()).isEqualTo(Category.FOOD);
            assertThat(response.getDescription()).isEqualTo("Lunch");
            verify(expenseRepository).save(any(Expense.class));
        }
    }

    @Nested
    @DisplayName("findAll()")
    class FindAllTests {

        @Test
        @DisplayName("should return paginated expenses without filters")
        void shouldReturnAllExpenses() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Expense> page = new PageImpl<>(List.of(testExpense));
            when(expenseRepository.findByUserId(1L, pageable)).thenReturn(page);

            Page<ExpenseResponse> result = expenseService.findAll(
                    1L, null, null, null, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getAmount())
                    .isEqualByComparingTo("25.50");
        }

        @Test
        @DisplayName("should filter by category")
        void shouldFilterByCategory() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Expense> page = new PageImpl<>(List.of(testExpense));
            when(expenseRepository.findByUserIdAndCategory(1L, Category.FOOD, pageable))
                    .thenReturn(page);

            Page<ExpenseResponse> result = expenseService.findAll(
                    1L, Category.FOOD, null, null, pageable);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("should filter by date range")
        void shouldFilterByDateRange() {
            Pageable pageable = PageRequest.of(0, 20);
            LocalDate start = LocalDate.of(2026, 3, 1);
            LocalDate end = LocalDate.of(2026, 3, 31);
            Page<Expense> page = new PageImpl<>(List.of(testExpense));
            when(expenseRepository.findByUserIdAndDateBetween(1L, start, end, pageable))
                    .thenReturn(page);

            Page<ExpenseResponse> result = expenseService.findAll(
                    1L, null, start, end, pageable);

            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindByIdTests {

        @Test
        @DisplayName("should return expense when found")
        void shouldReturnExpenseWhenFound() {
            when(expenseRepository.findByIdAndUserId(1L, 1L))
                    .thenReturn(Optional.of(testExpense));

            ExpenseResponse response = expenseService.findById(1L, 1L);

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getCategory()).isEqualTo(Category.FOOD);
        }

        @Test
        @DisplayName("should throw when expense not found")
        void shouldThrowWhenNotFound() {
            when(expenseRepository.findByIdAndUserId(99L, 1L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> expenseService.findById(99L, 1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Expense not found");
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateTests {

        @Test
        @DisplayName("should update expense fields")
        void shouldUpdateExpense() {
            ExpenseRequest updateRequest = ExpenseRequest.builder()
                    .amount(new BigDecimal("50.00"))
                    .category(Category.TRANSPORT)
                    .description("Taxi ride")
                    .date(LocalDate.of(2026, 3, 16))
                    .build();

            Expense updatedExpense = Expense.builder()
                    .id(1L)
                    .amount(new BigDecimal("50.00"))
                    .category(Category.TRANSPORT)
                    .description("Taxi ride")
                    .date(LocalDate.of(2026, 3, 16))
                    .user(testUser)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(expenseRepository.findByIdAndUserId(1L, 1L))
                    .thenReturn(Optional.of(testExpense));
            when(expenseRepository.save(any(Expense.class))).thenReturn(updatedExpense);

            ExpenseResponse response = expenseService.update(1L, updateRequest, 1L);

            assertThat(response.getAmount()).isEqualByComparingTo("50.00");
            assertThat(response.getCategory()).isEqualTo(Category.TRANSPORT);
        }

        @Test
        @DisplayName("should throw when updating non-existent expense")
        void shouldThrowWhenUpdatingNonExistent() {
            when(expenseRepository.findByIdAndUserId(99L, 1L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> expenseService.update(99L, testRequest, 1L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("should delete expense")
        void shouldDeleteExpense() {
            when(expenseRepository.findByIdAndUserId(1L, 1L))
                    .thenReturn(Optional.of(testExpense));

            expenseService.delete(1L, 1L);

            verify(expenseRepository).delete(testExpense);
        }

        @Test
        @DisplayName("should throw when deleting non-existent expense")
        void shouldThrowWhenDeletingNonExistent() {
            when(expenseRepository.findByIdAndUserId(99L, 1L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> expenseService.delete(99L, 1L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
