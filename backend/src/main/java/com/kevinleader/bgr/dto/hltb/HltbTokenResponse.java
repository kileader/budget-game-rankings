package com.kevinleader.bgr.dto.hltb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HltbTokenResponse(String token, String hpKey, String hpVal) {}
