package com.kevinleader.bgr.dto.hltb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HltbSearchResponse(List<HltbGameResult> data) {}
