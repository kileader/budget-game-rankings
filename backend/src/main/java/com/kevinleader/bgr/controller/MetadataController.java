package com.kevinleader.bgr.controller;

import com.kevinleader.bgr.dto.metadata.MetadataItemDto;
import com.kevinleader.bgr.service.MetadataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/metadata")
public class MetadataController {

    private final MetadataService metadataService;

    public MetadataController(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @GetMapping("/platforms")
    public List<MetadataItemDto> getPlatforms() {
        return metadataService.getPlatforms();
    }

    @GetMapping("/genres")
    public List<MetadataItemDto> getGenres() {
        return metadataService.getGenres();
    }
}
