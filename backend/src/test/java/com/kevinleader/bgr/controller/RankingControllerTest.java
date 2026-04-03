package com.kevinleader.bgr.controller;

import com.kevinleader.bgr.dto.ranking.RankingPageDto;
import com.kevinleader.bgr.dto.ranking.RankingQueryDto;
import com.kevinleader.bgr.dto.ranking.RankingResultDto;
import com.kevinleader.bgr.dto.ranking.RankingSort;
import com.kevinleader.bgr.exception.GlobalExceptionHandler;
import com.kevinleader.bgr.service.RankingService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RankingControllerTest {

    @Test
    void returnsPagedRankingResponse() throws Exception {
        RankingService rankingService = mock(RankingService.class);
        MockMvc mockMvc = buildMockMvc(rankingService);

        when(rankingService.getRankingsPage(any())).thenReturn(new RankingPageDto(
                0,
                2,
                10,
                List.of(
                        new RankingResultDto(
                                1L,
                                "Game One",
                                new BigDecimal("90.00"),
                                new BigDecimal("12.00"),
                                1999,
                                new BigDecimal("54.0270"),
                                "https://example.com/cover.jpg",
                                "https://igdb.com/game-one",
                                "https://cheapshark.com/deal/1"
                        ),
                        new RankingResultDto(
                                2L,
                                "Game Two",
                                new BigDecimal("85.00"),
                                new BigDecimal("10.00"),
                                2499,
                                new BigDecimal("34.0136"),
                                null,
                                "https://igdb.com/game-two",
                                null
                        )
                )
        ));

        mockMvc.perform(get("/rankings")
                        .param("limit", "2")
                        .param("sort", "VALUE_SCORE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.offset").value(0))
                .andExpect(jsonPath("$.limit").value(2))
                .andExpect(jsonPath("$.total").value(10))
                .andExpect(jsonPath("$.results[0].igdbGameId").value(1))
                .andExpect(jsonPath("$.results[0].title").value("Game One"))
                .andExpect(jsonPath("$.results[0].priceCents").value(1999))
                .andExpect(jsonPath("$.results[1].igdbGameId").value(2));
    }

    @Test
    void bindsQueryParametersIntoRankingQueryDto() throws Exception {
        RankingService rankingService = mock(RankingService.class);
        MockMvc mockMvc = buildMockMvc(rankingService);

        when(rankingService.getRankingsPage(any())).thenReturn(new RankingPageDto(5, 25, 0, List.of()));

        mockMvc.perform(get("/rankings")
                        .param("platformIds", "130", "167")
                        .param("genreIds", "12")
                        .param("releaseYearMin", "2020")
                        .param("releaseYearMax", "2024")
                        .param("minPriceCents", "1000")
                        .param("maxPriceCents", "4000")
                        .param("minPlaytimeHours", "5.5")
                        .param("maxPlaytimeHours", "25.0")
                        .param("sort", "PRICE")
                        .param("offset", "5")
                        .param("limit", "25"))
                .andExpect(status().isOk());

        ArgumentCaptor<RankingQueryDto> captor = ArgumentCaptor.forClass(RankingQueryDto.class);
        verify(rankingService).getRankingsPage(captor.capture());

        RankingQueryDto query = captor.getValue();
        assertThat(query.platformIds()).containsExactly(130, 167);
        assertThat(query.genreIds()).containsExactly(12);
        assertThat(query.releaseYearMin()).isEqualTo(2020);
        assertThat(query.releaseYearMax()).isEqualTo(2024);
        assertThat(query.minPriceCents()).isEqualTo(1000);
        assertThat(query.maxPriceCents()).isEqualTo(4000);
        assertThat(query.minPlaytimeHours()).isEqualByComparingTo("5.5");
        assertThat(query.maxPlaytimeHours()).isEqualByComparingTo("25.0");
        assertThat(query.sort()).isEqualTo(RankingSort.PRICE);
        assertThat(query.offset()).isEqualTo(5);
        assertThat(query.limit()).isEqualTo(25);
    }

    @Test
    void rendersBadRequestErrorsFromServiceValidation() throws Exception {
        RankingService rankingService = mock(RankingService.class);
        MockMvc mockMvc = buildMockMvc(rankingService);
        when(rankingService.getRankingsPage(any()))
                .thenThrow(new IllegalArgumentException("limit must be between 1 and 500"));

        mockMvc.perform(get("/rankings")
                        .param("limit", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("limit must be between 1 and 500"))
                .andExpect(jsonPath("$.path").value("/rankings"));
    }

    private MockMvc buildMockMvc(RankingService rankingService) {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        return MockMvcBuilders.standaloneSetup(new RankingController(rankingService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .setValidator(validator)
                .build();
    }
}
