package com.kevinleader.bgr.service;

import com.kevinleader.bgr.dto.auth.AuthRequestDto;
import com.kevinleader.bgr.dto.auth.AuthResponseDto;
import com.kevinleader.bgr.dto.auth.SignupRequestDto;
import com.kevinleader.bgr.entity.AppUser;
import com.kevinleader.bgr.repository.AppUserRepository;
import com.kevinleader.bgr.security.AppUserPrincipal;
import com.kevinleader.bgr.security.JwtService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private static final String TEST_SECRET = "test-secret-key-with-sufficient-length-1234567890";

    @Test
    void signupCreatesUserAndReturnsToken() {
        AppUserRepository repository = mock(AppUserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        JwtService jwtService = new JwtService(TEST_SECRET, 60_000);
        AuthService authService = new AuthService(repository, passwordEncoder, authenticationManager, jwtService);

        when(repository.existsByUsername("kevin")).thenReturn(false);
        when(repository.existsByEmail("kevin@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");

        AuthResponseDto response = authService.signup(new SignupRequestDto(
                "kevin",
                "kevin@example.com",
                "password123"
        ));

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(repository).save(captor.capture());

        AppUser saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo("kevin");
        assertThat(saved.getEmail()).isEqualTo("kevin@example.com");
        assertThat(saved.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(saved.getRole()).isEqualTo("USER");
        assertThat(saved.isActive()).isTrue();

        assertThat(response.username()).isEqualTo("kevin");
        assertThat(response.role()).isEqualTo("USER");
        assertThat(response.token()).isNotBlank();
    }

    @Test
    void loginReturnsTokenForValidCredentials() {
        AppUserRepository repository = mock(AppUserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        JwtService jwtService = new JwtService(TEST_SECRET, 60_000);
        AuthService authService = new AuthService(repository, passwordEncoder, authenticationManager, jwtService);

        AppUser user = new AppUser();
        user.setUsername("kevin");
        user.setRole("USER");
        user.setActive(true);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                new AppUserPrincipal(user),
                null,
                new AppUserPrincipal(user).getAuthorities()
        );

        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        AuthResponseDto response = authService.login(new AuthRequestDto("kevin", "password123"));

        assertThat(response.username()).isEqualTo("kevin");
        assertThat(response.role()).isEqualTo("USER");
        assertThat(response.token()).isNotBlank();
    }

    @Test
    void loginRejectsBadCredentials() {
        AppUserRepository repository = mock(AppUserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        JwtService jwtService = new JwtService(TEST_SECRET, 60_000);
        AuthService authService = new AuthService(repository, passwordEncoder, authenticationManager, jwtService);

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad credentials"));

        assertThatThrownBy(() -> authService.login(new AuthRequestDto("kevin", "wrong-password")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid username or password");
    }
}
