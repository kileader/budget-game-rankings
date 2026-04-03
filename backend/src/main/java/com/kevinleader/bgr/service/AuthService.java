package com.kevinleader.bgr.service;

import com.kevinleader.bgr.dto.auth.AuthRequestDto;
import com.kevinleader.bgr.dto.auth.AuthResponseDto;
import com.kevinleader.bgr.dto.auth.SignupRequestDto;
import com.kevinleader.bgr.entity.AppUser;
import com.kevinleader.bgr.repository.AppUserRepository;
import com.kevinleader.bgr.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(AppUserRepository appUserRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthResponseDto signup(SignupRequestDto request) {
        if (appUserRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username is already taken");
        }
        if (appUserRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        AppUser user = new AppUser();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole("USER");
        user.setActive(true);
        appUserRepository.save(user);

        String token = jwtService.generateToken(user.getUsername(), user.getRole());
        return new AuthResponseDto(token, user.getUsername(), user.getRole());
    }

    public AuthResponseDto login(AuthRequestDto request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
            Object principal = authentication.getPrincipal();
            if (principal instanceof com.kevinleader.bgr.security.AppUserPrincipal appUserPrincipal) {
                AppUser user = appUserPrincipal.getUser();
                String token = jwtService.generateToken(user.getUsername(), user.getRole());
                return new AuthResponseDto(token, user.getUsername(), user.getRole());
            }
        } catch (BadCredentialsException ex) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        throw new IllegalArgumentException("Authentication failed");
    }
}
