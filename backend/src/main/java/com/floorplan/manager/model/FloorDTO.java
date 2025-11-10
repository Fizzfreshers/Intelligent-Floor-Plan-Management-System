package com.floorplan.manager.model;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FloorDTO {
    private int floorNumber;
    private List<RoomDTO> rooms;
    
    private transient String username;
    private transient String versionTag;
}