package com.shivaji.expensetracker.controller;

import com.shivaji.expensetracker.dto.AnalyticsResponse;
import com.shivaji.expensetracker.model.User;
import com.shivaji.expensetracker.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Expense analytics and reporting with Redis caching")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping
    @Operation(summary = "Get expense analytics for a date range",
            description = "Returns total spend, category breakdown, and monthly trends. "
                    + "Responses are cached in Redis for 30 minutes.")
    public ResponseEntity<AnalyticsResponse> getAnalytics(
            @AuthenticationPrincipal User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must be before or equal to endDate");
        }

        return ResponseEntity.ok(
                analyticsService.getAnalytics(user.getId(), startDate, endDate));
    }

    @GetMapping("/monthly")
    @Operation(summary = "Get current month analytics")
    public ResponseEntity<AnalyticsResponse> getCurrentMonth(
            @AuthenticationPrincipal User user) {

        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);

        return ResponseEntity.ok(
                analyticsService.getAnalytics(user.getId(), startOfMonth, now));
    }

    @GetMapping("/yearly")
    @Operation(summary = "Get current year analytics")
    public ResponseEntity<AnalyticsResponse> getCurrentYear(
            @AuthenticationPrincipal User user) {

        LocalDate now = LocalDate.now();
        LocalDate startOfYear = now.withDayOfYear(1);

        return ResponseEntity.ok(
                analyticsService.getAnalytics(user.getId(), startOfYear, now));
    }
}
