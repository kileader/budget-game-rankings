package com.kevinleader.bgr.dto.auth;

public record AuthResponseDto(
        String token,
        String username,
        String role
) {
}
