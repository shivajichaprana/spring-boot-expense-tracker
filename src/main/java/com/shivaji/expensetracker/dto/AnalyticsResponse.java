package com.shivaji.expensetracker.dto;

import com.shivaji.expensetracker.model.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResponse implements Serializable {

    private BigDecimal totalAmount;
    private int totalTransactions;
    private BigDecimal averageAmount;
    private Map<Category, BigDecimal> byCategory;
    private List<MonthlyTrend> monthlyTrends;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyTrend implements Serializable {
        private int year;
        private int month;
        private BigDecimal total;
        private int count;
    }
}
