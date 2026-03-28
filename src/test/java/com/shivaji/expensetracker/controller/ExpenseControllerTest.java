package com.shivaji.expensetracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shivaji.expensetracker.dto.ExpenseRequest;
import com.shivaji.expensetracker.dto.ExpenseResponse;
import com.shivaji.expensetracker.model.Category;
import com.shivaji.expensetracker.model.User;
import com.shivaji.expensetracker.security.JwtTokenProvider;
import com.shivaji.expensetracker.service.AuthService;
import com.shivaji.expensetracker.service.ExpenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpenseController.class)
@ActiveProfiles("test")
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExpenseService expenseService;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private ObjectMapper objectMapper;
    private User testUser;
    private ExpenseResponse testResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .password("encoded-password")
                .build();

        testResponse = ExpenseResponse.builder()
                .id(1L)
                .amount(new BigDecimal("25.50"))
                .category(Category.FOOD)
                .description("Lunch")
                .date(LocalDate.of(2026, 3, 15))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/expenses - should create expense")
    void shouldCreateExpense() throws Exception {
        ExpenseRequest request = ExpenseRequest.builder()
                .amount(new BigDecimal("25.50"))
                .category(Category.FOOD)
                .description("Lunch")
                .date(LocalDate.of(2026, 3, 15))
                .build();

        when(expenseService.create(any(ExpenseRequest.class), any(User.class)))
                .thenReturn(testResponse);

        mockMvc.perform(post("/api/v1/expenses")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(25.50))
                .andExpect(jsonPath("$.category").value("FOOD"));
    }

    @Test
    @DisplayName("POST /api/v1/expenses - should reject invalid request")
    void shouldRejectInvalidRequest() throws Exception {
        ExpenseRequest invalid = ExpenseRequest.builder()
                .amount(null)
                .category(null)
                .date(null)
                .build();

        mockMvc.perform(post("/api/v1/expenses")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/expenses - should return paginated list")
    void shouldReturnPaginatedExpenses() throws Exception {
        when(expenseService.findAll(eq(1L), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(testResponse)));

        mockMvc.perform(get("/api/v1/expenses")
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].amount").value(25.50));
    }

    @Test
    @DisplayName("GET /api/v1/expenses/{id} - should return expense")
    void shouldReturnExpenseById() throws Exception {
        when(expenseService.findById(1L, 1L)).thenReturn(testResponse);

        mockMvc.perform(get("/api/v1/expenses/1")
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Lunch"));
    }

    @Test
    @DisplayName("DELETE /api/v1/expenses/{id} - should delete expense")
    void shouldDeleteExpense() throws Exception {
        mockMvc.perform(delete("/api/v1/expenses/1")
                        .with(user(testUser))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Unauthenticated request should return 401")
    void shouldRejectUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/expenses"))
                .andExpect(status().isUnauthorized());
    }
}
