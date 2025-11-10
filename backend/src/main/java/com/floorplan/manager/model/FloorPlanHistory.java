package com.floorplan.manager.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "floor_plan_history",
       indexes = @Index(name = "idx_version_tag", columnList = "versionTag"))
@Data
@NoArgsConstructor
public class FloorPlanHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // the primary key

    @Column(nullable = false)
    private String versionTag; // the user-provided tag

    @Column(nullable = false)
    private String username;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String floorPlanData;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}