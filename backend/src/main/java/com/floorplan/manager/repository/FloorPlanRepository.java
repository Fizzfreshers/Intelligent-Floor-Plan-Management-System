package com.floorplan.manager.repository;

import com.floorplan.manager.model.FloorPlanHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FloorPlanRepository extends JpaRepository<FloorPlanHistory, Long> {
    Optional<FloorPlanHistory> findFirstByVersionTagOrderByTimestampDesc(String versionTag); //finds the most recent floor plan entry for a given version tag
    List<FloorPlanHistory> findByVersionTagOrderByTimestampDesc(String versionTag); //finds all entries for a given version tag in history, ordered by time
}