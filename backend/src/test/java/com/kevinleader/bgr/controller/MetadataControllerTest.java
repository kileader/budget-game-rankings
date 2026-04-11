package com.kevinleader.bgr.controller;

import com.kevinleader.bgr.dto.metadata.MetadataItemDto;
import com.kevinleader.bgr.exception.GlobalExceptionHandler;
import com.kevinleader.bgr.service.MetadataService;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MetadataControllerTest {

    private MockMvc buildMockMvc(MetadataService service) {
        return MockMvcBuilders.standaloneSetup(new MetadataController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new JacksonJsonHttpMessageConverter())
                .build();
    }

    @Test
    void platformsReturnsList() throws Exception {
        MetadataService service = mock(MetadataService.class);
        when(service.getPlatforms()).thenReturn(List.of(new MetadataItemDto(6, "PC (Windows)")));

        buildMockMvc(service).perform(get("/metadata/platforms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(6))
                .andExpect(jsonPath("$[0].name").value("PC (Windows)"));
    }

    @Test
    void genresReturnsList() throws Exception {
        MetadataService service = mock(MetadataService.class);
        when(service.getGenres()).thenReturn(List.of(new MetadataItemDto(12, "Role-playing (RPG)")));

        buildMockMvc(service).perform(get("/metadata/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(12))
                .andExpect(jsonPath("$[0].name").value("Role-playing (RPG)"));
    }

    @Test
    void platformsReturnsEmptyList() throws Exception {
        MetadataService service = mock(MetadataService.class);
        when(service.getPlatforms()).thenReturn(List.of());

        buildMockMvc(service).perform(get("/metadata/platforms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void genresReturnsEmptyList() throws Exception {
        MetadataService service = mock(MetadataService.class);
        when(service.getGenres()).thenReturn(List.of());

        buildMockMvc(service).perform(get("/metadata/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
