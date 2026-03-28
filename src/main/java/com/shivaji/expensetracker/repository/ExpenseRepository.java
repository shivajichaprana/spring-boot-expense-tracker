package com.shivaji.expensetracker.repository;

import com.shivaji.expensetracker.model.Category;
import com.shivaji.expensetracker.model.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    Page<Expense> findByUserId(Long userId, Pageable pageable);

    Optional<Expense> findByIdAndUserId(Long id, Long userId);

    Page<Expense> findByUserIdAndCategory(Long userId, Category category, Pageable pageable);

    Page<Expense> findByUserIdAndDateBetween(Long userId, LocalDate start, LocalDate end, Pageable pageable);

    Page<Expense> findByUserIdAndCategoryAndDateBetween(
            Long userId, Category category, LocalDate start, LocalDate end, Pageable pageable);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
            "WHERE e.user.id = :userId AND e.date BETWEEN :start AND :end")
    BigDecimal sumAmountByUserIdAndDateBetween(
            @Param("userId") Long userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query("SELECT e.category, COALESCE(SUM(e.amount), 0) FROM Expense e " +
            "WHERE e.user.id = :userId AND e.date BETWEEN :start AND :end " +
            "GROUP BY e.category ORDER BY SUM(e.amount) DESC")
    List<Object[]> sumByCategory(
            @Param("userId") Long userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query("SELECT YEAR(e.date), MONTH(e.date), COALESCE(SUM(e.amount), 0), COUNT(e) " +
            "FROM Expense e WHERE e.user.id = :userId AND e.date BETWEEN :start AND :end " +
            "GROUP BY YEAR(e.date), MONTH(e.date) ORDER BY YEAR(e.date), MONTH(e.date)")
    List<Object[]> monthlyTrends(
            @Param("userId") Long userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    long countByUserIdAndDateBetween(Long userId, LocalDate start, LocalDate end);

    void deleteByIdAndUserId(Long id, Long userId);
}
