package com.kevinleader.bgr.controller;

import com.kevinleader.bgr.dto.auth.AuthResponseDto;
import com.kevinleader.bgr.dto.auth.CurrentUserDto;
import com.kevinleader.bgr.entity.AppUser;
import com.kevinleader.bgr.exception.GlobalExceptionHandler;
import com.kevinleader.bgr.security.AppUserPrincipal;
import com.kevinleader.bgr.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    @Test
    void signupReturnsTokenPayload() throws Exception {
        AuthService authService = mock(AuthService.class);
        MockMvc mockMvc = buildMockMvc(authService);

        when(authService.signup(any())).thenReturn(new AuthResponseDto(
                "token-123",
                "kevin",
                "USER"
        ));

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "kevin",
                                  "email": "kevin@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-123"))
                .andExpect(jsonPath("$.username").value("kevin"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(authService).signup(any());
    }

    @Test
    void loginReturnsTokenPayload() throws Exception {
        AuthService authService = mock(AuthService.class);
        MockMvc mockMvc = buildMockMvc(authService);

        when(authService.login(any())).thenReturn(new AuthResponseDto(
                "token-456",
                "kevin",
                "USER"
        ));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "kevin",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-456"))
                .andExpect(jsonPath("$.username").value("kevin"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(authService).login(any());
    }

    @Test
    void signupRejectsInvalidRequestBody() throws Exception {
        AuthService authService = mock(AuthService.class);
        MockMvc mockMvc = buildMockMvc(authService);

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "ab",
                                  "email": "not-an-email",
                                  "password": "short"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/auth/signup"));
    }

    @Test
    void loginRendersBadCredentialsAsBadRequest() throws Exception {
        AuthService authService = mock(AuthService.class);
        MockMvc mockMvc = buildMockMvc(authService);
        when(authService.login(any())).thenThrow(new IllegalArgumentException("Invalid username or password"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "kevin",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid username or password"))
                .andExpect(jsonPath("$.path").value("/auth/login"));
    }

    @Test
    void meReturnsCurrentAuthenticatedUser() throws Exception {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService);

        AppUser user = new AppUser();
        user.setId(7L);
        user.setUsername("kevin");
        user.setEmail("kevin@example.com");
        user.setRole("USER");
        user.setActive(true);

        CurrentUserDto response = controller.me(new AppUserPrincipal(user));

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.username()).isEqualTo("kevin");
        assertThat(response.email()).isEqualTo("kevin@example.com");
        assertThat(response.role()).isEqualTo("USER");
    }

    @Test
    void meRejectsAnonymousRequest() {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService);

        assertThatThrownBy(() -> controller.me(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Authentication required");
    }

    private MockMvc buildMockMvc(AuthService authService) {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        return MockMvcBuilders.standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .setValidator(validator)
                .build();
    }
}
