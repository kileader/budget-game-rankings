package com.kevinleader.bgr.dto.auth;

public record CurrentUserDto(
        Long id,
        String username,
        String email,
        String role
) {
}
