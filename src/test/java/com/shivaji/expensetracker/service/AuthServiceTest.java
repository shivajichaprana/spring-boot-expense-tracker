package com.shivaji.expensetracker.service;

import com.shivaji.expensetracker.dto.AuthRequest;
import com.shivaji.expensetracker.dto.AuthResponse;
import com.shivaji.expensetracker.exception.DuplicateResourceException;
import com.shivaji.expensetracker.model.User;
import com.shivaji.expensetracker.repository.UserRepository;
import com.shivaji.expensetracker.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .password("encoded-password")
                .build();
    }

    @Test
    @DisplayName("register - should create user and return tokens")
    void shouldRegisterUser() {
        AuthRequest.Register request = AuthRequest.Register.builder()
                .name("Test User")
                .email("test@example.com")
                .password("password123")
                .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtTokenProvider.generateAccessToken(any())).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(any())).thenReturn("refresh-token");
        when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(900000L);

        AuthResponse response = authService.register(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register - should throw on duplicate email")
    void shouldThrowOnDuplicateEmail() {
        AuthRequest.Register request = AuthRequest.Register.builder()
                .name("Test User")
                .email("test@example.com")
                .password("password123")
                .build();

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("login - should authenticate and return tokens")
    void shouldLoginUser() {
        AuthRequest.Login request = AuthRequest.Login.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateAccessToken(any())).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(any())).thenReturn("refresh-token");
        when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(900000L);

        AuthResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
    }

    @Test
    @DisplayName("loadUserByUsername - should throw when user not found")
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.loadUserByUsername("unknown@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
