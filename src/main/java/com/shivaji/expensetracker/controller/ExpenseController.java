package com.shivaji.expensetracker.controller;

import com.shivaji.expensetracker.dto.ExpenseRequest;
import com.shivaji.expensetracker.dto.ExpenseResponse;
import com.shivaji.expensetracker.model.Category;
import com.shivaji.expensetracker.model.User;
import com.shivaji.expensetracker.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "CRUD operations for expense management")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    @Operation(summary = "Create a new expense")
    public ResponseEntity<ExpenseResponse> create(
            @Valid @RequestBody ExpenseRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(expenseService.create(request, user));
    }

    @GetMapping
    @Operation(summary = "List expenses with optional filters and pagination")
    public ResponseEntity<Page<ExpenseResponse>> findAll(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(
                expenseService.findAll(user.getId(), category, startDate, endDate, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a specific expense by ID")
    public ResponseEntity<ExpenseResponse> findById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(expenseService.findById(id, user.getId()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing expense")
    public ResponseEntity<ExpenseResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(expenseService.update(id, request, user.getId()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an expense")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        expenseService.delete(id, user.getId());
    }
}
