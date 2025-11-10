package com.floorplan.manager.model;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateRequestDTO {
    private String username;
    private String version; //maps to the 'versionTag'
    private List<FloorDTO> floorDTOs;
}