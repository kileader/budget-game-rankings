package com.kevinleader.bgr.controller;

import com.kevinleader.bgr.dto.config.RankingConfigDto;
import com.kevinleader.bgr.dto.config.RankingConfigRequestDto;
import com.kevinleader.bgr.entity.AppUser;
import com.kevinleader.bgr.exception.GlobalExceptionHandler;
import com.kevinleader.bgr.security.AppUserPrincipal;
import com.kevinleader.bgr.service.RankingConfigService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RankingConfigControllerTest {

    private AppUserPrincipal principal() {
        AppUser user = new AppUser();
        user.setUsername("kevin");
        user.setEmail("kevin@example.com");
        user.setRole("USER");
        user.setActive(true);
        return new AppUserPrincipal(user);
    }

    private RankingConfigDto sampleDto() {
        return new RankingConfigDto(1L, "Budget RPGs", List.of(), List.of(),
                null, null, 0, 2000, null, null, null, null);
    }

    private MockMvc buildMockMvc(RankingConfigService service) {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        return MockMvcBuilders.standaloneSetup(new RankingConfigController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .setValidator(validator)
                .build();
    }

    @Test
    void listReturnsConfigs() {
        RankingConfigService service = mock(RankingConfigService.class);
        RankingConfigController controller = new RankingConfigController(service);
        AppUserPrincipal principal = principal();

        when(service.listConfigs(principal.getUser())).thenReturn(List.of(sampleDto()));

        List<RankingConfigDto> result = controller.list(principal);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Budget RPGs");
    }

    @Test
    void createReturns201() throws Exception {
        RankingConfigService service = mock(RankingConfigService.class);
        MockMvc mockMvc = buildMockMvc(service);

        when(service.createConfig(any(), any())).thenReturn(sampleDto());

        mockMvc.perform(post("/users/me/ranking-configs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Budget RPGs",
                                  "maxPriceCents": 2000
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Budget RPGs"));
    }

    @Test
    void createRejectsMissingName() throws Exception {
        RankingConfigService service = mock(RankingConfigService.class);
        MockMvc mockMvc = buildMockMvc(service);

        mockMvc.perform(post("/users/me/ranking-configs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "maxPriceCents": 2000
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteReturns204() throws Exception {
        RankingConfigService service = mock(RankingConfigService.class);
        MockMvc mockMvc = buildMockMvc(service);

        mockMvc.perform(delete("/users/me/ranking-configs/1"))
                .andExpect(status().isNoContent());

        verify(service).deleteConfig(any(), eq(1L));
    }

    @Test
    void updateReturnsUpdatedDto() throws Exception {
        RankingConfigService service = mock(RankingConfigService.class);
        MockMvc mockMvc = buildMockMvc(service);

        RankingConfigDto updated = new RankingConfigDto(1L, "Cheap Shooters", List.of(), List.of(),
                null, null, 0, 1500, null, null, null, null);
        when(service.updateConfig(any(), eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/users/me/ranking-configs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Cheap Shooters",
                                  "maxPriceCents": 1500
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cheap Shooters"))
                .andExpect(jsonPath("$.maxPriceCents").value(1500));
    }
}
