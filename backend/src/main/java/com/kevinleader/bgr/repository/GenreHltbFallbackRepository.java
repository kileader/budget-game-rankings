package com.kevinleader.bgr.repository;

import com.kevinleader.bgr.entity.GenreHltbFallback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GenreHltbFallbackRepository extends JpaRepository<GenreHltbFallback, Integer> {

    Optional<GenreHltbFallback> findByGenreName(String genreName);
}
