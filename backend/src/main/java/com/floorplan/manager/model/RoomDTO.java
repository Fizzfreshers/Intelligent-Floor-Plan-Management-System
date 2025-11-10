package com.floorplan.manager.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RoomDTO {
    private String roomId;
    private String name;
    private int capacity;
    private Boolean isAvailable;
    private int bookingWeightage;
}