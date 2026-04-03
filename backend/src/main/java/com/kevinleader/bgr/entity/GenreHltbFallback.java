package com.kevinleader.bgr.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "genre_hltb_fallback")
@Getter @Setter @NoArgsConstructor
public class GenreHltbFallback {

    @Id
    @Column(name = "igdb_genre_id")
    private Integer igdbGenreId;

    @Column(name = "genre_name", nullable = false, length = 100)
    private String genreName;

    @Column(name = "avg_hours", nullable = false, precision = 6, scale = 2)
    private BigDecimal avgHours;
}
