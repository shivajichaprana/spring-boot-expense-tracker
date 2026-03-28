package com.shivaji.expensetracker.repository;

import com.shivaji.expensetracker.model.Category;
import com.shivaji.expensetracker.model.Expense;
import com.shivaji.expensetracker.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ExpenseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ExpenseRepository expenseRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .password("encoded-password")
                .name("Test User")
                .build();
        testUser = entityManager.persistAndFlush(testUser);

        Expense expense1 = Expense.builder()
                .amount(new BigDecimal("25.50"))
                .category(Category.FOOD)
                .description("Lunch")
                .date(LocalDate.of(2026, 3, 15))
                .user(testUser)
                .build();

        Expense expense2 = Expense.builder()
                .amount(new BigDecimal("1500.00"))
                .category(Category.HOUSING)
                .description("Rent")
                .date(LocalDate.of(2026, 3, 1))
                .user(testUser)
                .build();

        Expense expense3 = Expense.builder()
                .amount(new BigDecimal("45.00"))
                .category(Category.FOOD)
                .description("Dinner")
                .date(LocalDate.of(2026, 2, 20))
                .user(testUser)
                .build();

        entityManager.persistAndFlush(expense1);
        entityManager.persistAndFlush(expense2);
        entityManager.persistAndFlush(expense3);
    }

    @Test
    @DisplayName("should find all expenses by user")
    void shouldFindByUserId() {
        Page<Expense> expenses = expenseRepository.findByUserId(
                testUser.getId(), PageRequest.of(0, 20));

        assertThat(expenses.getContent()).hasSize(3);
    }

    @Test
    @DisplayName("should find expenses by category")
    void shouldFindByCategory() {
        Page<Expense> expenses = expenseRepository.findByUserIdAndCategory(
                testUser.getId(), Category.FOOD, PageRequest.of(0, 20));

        assertThat(expenses.getContent()).hasSize(2);
        assertThat(expenses.getContent())
                .allMatch(e -> e.getCategory() == Category.FOOD);
    }

    @Test
    @DisplayName("should find expenses by date range")
    void shouldFindByDateRange() {
        Page<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(
                testUser.getId(),
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31),
                PageRequest.of(0, 20));

        assertThat(expenses.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("should find expense by id and user id")
    void shouldFindByIdAndUserId() {
        Page<Expense> all = expenseRepository.findByUserId(
                testUser.getId(), PageRequest.of(0, 1));
        Long expenseId = all.getContent().get(0).getId();

        Optional<Expense> found = expenseRepository.findByIdAndUserId(
                expenseId, testUser.getId());

        assertThat(found).isPresent();
    }

    @Test
    @DisplayName("should sum amount by date range")
    void shouldSumAmount() {
        BigDecimal total = expenseRepository.sumAmountByUserIdAndDateBetween(
                testUser.getId(),
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31));

        assertThat(total).isEqualByComparingTo("1525.50");
    }

    @Test
    @DisplayName("should get category breakdown")
    void shouldGetCategoryBreakdown() {
        List<Object[]> results = expenseRepository.sumByCategory(
                testUser.getId(),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31));

        assertThat(results).hasSize(2); // FOOD and HOUSING
    }

    @Test
    @DisplayName("should return empty for non-existent user")
    void shouldReturnEmptyForNonExistentUser() {
        Page<Expense> expenses = expenseRepository.findByUserId(
                999L, PageRequest.of(0, 20));

        assertThat(expenses.getContent()).isEmpty();
    }
}
