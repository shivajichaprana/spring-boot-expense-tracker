package com.shivaji.expensetracker.service;

import com.shivaji.expensetracker.dto.AuthRequest;
import com.shivaji.expensetracker.dto.AuthResponse;
import com.shivaji.expensetracker.exception.DuplicateResourceException;
import com.shivaji.expensetracker.model.User;
import com.shivaji.expensetracker.repository.UserRepository;
import com.shivaji.expensetracker.security.JwtTokenProvider;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       @Lazy AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(AuthRequest.Register request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "User with email '" + request.getEmail() + "' already exists");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        user = userRepository.save(user);

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        return AuthResponse.of(
                accessToken, refreshToken,
                jwtTokenProvider.getAccessTokenExpiration(),
                user.getEmail(), user.getName());
    }

    public AuthResponse login(AuthRequest.Login request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        return AuthResponse.of(
                accessToken, refreshToken,
                jwtTokenProvider.getAccessTokenExpiration(),
                user.getEmail(), user.getName());
    }

    public AuthResponse refreshToken(AuthRequest.RefreshToken request) {
        String token = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(token) || !jwtTokenProvider.isRefreshToken(token)) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        String email = jwtTokenProvider.extractUsername(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);

        return AuthResponse.of(
                accessToken, newRefreshToken,
                jwtTokenProvider.getAccessTokenExpiration(),
                user.getEmail(), user.getName());
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email: " + email));
    }
}
