package com.floorplan.manager.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import com.floorplan.manager.service.FloorPlanService;
import com.floorplan.manager.model.FloorDTO;
import com.floorplan.manager.model.UpdateRequestDTO;
import com.floorplan.manager.model.RoomDTO;
import com.floorplan.manager.model.MeetingRequestDTO;

@RestController
@RequestMapping("/api/floorplan")
public class FloorPlanController {

    private final FloorPlanService floorPlanService;

    public FloorPlanController(FloorPlanService floorPlanService) {
        this.floorPlanService = floorPlanService;
    }

    // endpoint to save a new floor plan
    @PostMapping("/update")
    public ResponseEntity<String> updateFloorPlan(@RequestBody UpdateRequestDTO updateRequestDTO) {
        if (updateRequestDTO.getUsername().equals("admin")) {
            floorPlanService.saveNewVersion(
                updateRequestDTO.getUsername(), 
                updateRequestDTO.getVersion(),
                updateRequestDTO.getFloorDTOs()
            );
            return ResponseEntity.ok("New floor plan version saved successfully");
        } else {
            return ResponseEntity.ok("Only admin user can save a new floor plan");
        }
    }

    // endpoint to get latest floor plan
    @GetMapping
    public ResponseEntity<List<FloorDTO>> getFloorPlans(
            @RequestParam String username,
            @RequestParam String version) {
        List<FloorDTO> floorDTOs = floorPlanService.getFloorPlansByVersion(version);
        return ResponseEntity.ok(floorDTOs);
    }

    // endpoint to get recommendations of rooms
    @GetMapping("/recommend-rooms")
    public ResponseEntity<List<RoomDTO>> recommendRooms(
            @RequestParam String versionTag,
            @RequestParam int participants,
            @RequestParam(required = false) String lastRoomName) {
        List<RoomDTO> recommendedRooms = floorPlanService.getRecommendedRooms(versionTag, participants, lastRoomName);
        return ResponseEntity.ok(recommendedRooms);
    }

    // endpoint to book a room
    @PostMapping("/book-room")
    public ResponseEntity<String> bookRoom(@RequestBody MeetingRequestDTO meetingRequestDTO) {
        String result = floorPlanService.bookRoom(
            meetingRequestDTO.getRoomName(), 
            meetingRequestDTO.getParticipants(),
            meetingRequestDTO.getVersion()
        );
        
        if (result.contains("successfully")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
}