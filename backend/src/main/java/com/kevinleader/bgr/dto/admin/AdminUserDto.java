package com.kevinleader.bgr.dto.admin;

import java.time.OffsetDateTime;

public record AdminUserDto(
        Long id,
        String username,
        String email,
        String role,
        boolean active,
        OffsetDateTime createdAt
) {
}
