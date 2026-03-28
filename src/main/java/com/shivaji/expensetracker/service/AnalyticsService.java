package com.shivaji.expensetracker.service;

import com.shivaji.expensetracker.dto.AnalyticsResponse;
import com.shivaji.expensetracker.model.Category;
import com.shivaji.expensetracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ExpenseRepository expenseRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "analytics", key = "#userId + '-' + #startDate + '-' + #endDate")
    public AnalyticsResponse getAnalytics(Long userId, LocalDate startDate, LocalDate endDate) {
        BigDecimal totalAmount = expenseRepository.sumAmountByUserIdAndDateBetween(
                userId, startDate, endDate);

        long totalTransactions = expenseRepository.countByUserIdAndDateBetween(
                userId, startDate, endDate);

        BigDecimal averageAmount = totalTransactions > 0
                ? totalAmount.divide(BigDecimal.valueOf(totalTransactions), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Map<Category, BigDecimal> byCategory = getCategoryBreakdown(userId, startDate, endDate);
        List<AnalyticsResponse.MonthlyTrend> trends = getMonthlyTrends(userId, startDate, endDate);

        return AnalyticsResponse.builder()
                .totalAmount(totalAmount)
                .totalTransactions((int) totalTransactions)
                .averageAmount(averageAmount)
                .byCategory(byCategory)
                .monthlyTrends(trends)
                .build();
    }

    private Map<Category, BigDecimal> getCategoryBreakdown(
            Long userId, LocalDate startDate, LocalDate endDate) {

        List<Object[]> results = expenseRepository.sumByCategory(userId, startDate, endDate);
        Map<Category, BigDecimal> breakdown = new EnumMap<>(Category.class);

        for (Object[] row : results) {
            Category category = (Category) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            breakdown.put(category, amount);
        }

        return breakdown;
    }

    private List<AnalyticsResponse.MonthlyTrend> getMonthlyTrends(
            Long userId, LocalDate startDate, LocalDate endDate) {

        List<Object[]> results = expenseRepository.monthlyTrends(userId, startDate, endDate);

        return results.stream()
                .map(row -> AnalyticsResponse.MonthlyTrend.builder()
                        .year((Integer) row[0])
                        .month((Integer) row[1])
                        .total((BigDecimal) row[2])
                        .count(((Long) row[3]).intValue())
                        .build())
                .toList();
    }
}
