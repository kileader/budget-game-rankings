package com.kevinleader.bgr.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "platform_ref")
@Getter @Setter @NoArgsConstructor
public class PlatformRef {

    @Id
    @Column(name = "igdb_platform_id")
    private Integer igdbPlatformId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
