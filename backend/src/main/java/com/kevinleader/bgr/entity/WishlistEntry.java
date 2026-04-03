package com.kevinleader.bgr.entity;

import jakarta.persistence.*;
import lombok.Getter;
<parameter name="content">import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(
    name = "wishlist_entry",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "igdb_game_id"})
)
@Getter @Setter @NoArgsConstructor
public class WishlistEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "igdb_game_id", nullable = false)
    private Long igdbGameId;

    @Column(name = "game_name", nullable = false)
    private String gameName;

    @Column(name = "added_at", nullable = false, updatable = false)
    private OffsetDateTime addedAt = OffsetDateTime.now();
}
