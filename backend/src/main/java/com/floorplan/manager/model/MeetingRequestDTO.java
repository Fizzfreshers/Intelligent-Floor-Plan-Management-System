package com.floorplan.manager.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MeetingRequestDTO {
    private String roomName;
    private int participants;
    private String version; //maps to the 'versionTag'
}