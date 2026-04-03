package com.kevinleader.bgr.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String TEST_SECRET = "test-secret-key-with-sufficient-length-1234567890";

    @Test
    void generatesAndParsesToken() {
        JwtService jwtService = new JwtService(TEST_SECRET, 60_000);

        String token = jwtService.generateToken("kevin", "USER");
        Claims claims = jwtService.parseToken(token);

        assertThat(claims.getSubject()).isEqualTo("kevin");
        assertThat(claims.get("role", String.class)).isEqualTo("USER");
        assertThat(claims.getExpiration()).isNotNull();
    }
}
