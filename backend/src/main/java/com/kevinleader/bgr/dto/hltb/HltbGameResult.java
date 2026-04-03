package com.kevinleader.bgr.dto.hltb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HltbGameResult(
        @JsonProperty("game_name") String gameName,
        @JsonProperty("comp_main") Integer compMain,
        @JsonProperty("comp_plus") Integer compPlus
) {}
