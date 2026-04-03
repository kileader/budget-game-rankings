package com.kevinleader.bgr.controller;

import com.kevinleader.bgr.dto.auth.AuthRequestDto;
import com.kevinleader.bgr.dto.auth.AuthResponseDto;
import com.kevinleader.bgr.dto.auth.CurrentUserDto;
import com.kevinleader.bgr.dto.auth.SignupRequestDto;
import com.kevinleader.bgr.security.AppUserPrincipal;
import com.kevinleader.bgr.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public AuthResponseDto signup(@Valid @RequestBody SignupRequestDto request) {
        return authService.signup(request);
    }

    @PostMapping("/login")
    public AuthResponseDto login(@Valid @RequestBody AuthRequestDto request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public CurrentUserDto me(@AuthenticationPrincipal AppUserPrincipal principal) {
        return new CurrentUserDto(
                principal.getUser().getId(),
                principal.getUser().getUsername(),
                principal.getUser().getEmail(),
                principal.getUser().getRole()
        );
    }
}
