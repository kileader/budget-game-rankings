package com.kevinleader.bgr.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record RoleUpdateRequestDto(
        @NotBlank
        String role
) {
}