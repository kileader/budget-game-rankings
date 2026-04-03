package com.kevinleader.bgr.dto.common;

public record ApiErrorDto(
        String timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
