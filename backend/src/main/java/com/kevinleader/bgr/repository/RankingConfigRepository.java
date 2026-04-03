package com.kevinleader.bgr.repository;

import com.kevinleader.bgr.entity.AppUser;
import com.kevinleader.bgr.entity.RankingConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RankingConfigRepository extends JpaRepository<RankingConfig, Long> {

    List<RankingConfig> findByUserOrderByCreatedAtDesc(AppUser user);

    Optional<RankingConfig> findByIdAndUser(Long id, AppUser user);

    boolean existsByUserAndName(AppUser user, String name);
}