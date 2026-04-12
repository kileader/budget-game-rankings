package com.kevinleader.bgr.repository;

import com.kevinleader.bgr.entity.PlatformRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlatformRefRepository extends JpaRepository<PlatformRef, Integer> {

    List<PlatformRef> findAllByOrderBySortOrderAsc();
}
