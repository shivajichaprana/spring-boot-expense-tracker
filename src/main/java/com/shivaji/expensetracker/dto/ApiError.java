package com.shivaji.expensetracker.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {
    private int status;
    private String message;
    private List<String> errors;
    private LocalDateTime timestamp;

    public static ApiError of(int status, String message) {
        return ApiError.builder()
                .status(status)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ApiError of(int status, String message, List<String> errors) {
        return ApiError.builder()
                .status(status)
                .message(message)
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
