package com.floorplan.manager.service;

import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

import com.floorplan.manager.repository.FloorPlanRepository;
import com.floorplan.manager.model.FloorPlanHistory;
import com.floorplan.manager.model.FloorDTO;
import com.floorplan.manager.model.RoomDTO;

@Service
public class FloorPlanService {

    private final FloorPlanRepository floorPlanRepository;
    private final ObjectMapper objectMapper;

    public FloorPlanService(FloorPlanRepository floorPlanRepository, ObjectMapper objectMapper) {
        this.floorPlanRepository = floorPlanRepository;
        this.objectMapper = objectMapper;
    }

    public void saveNewVersion(String username, String versionTag, List<FloorDTO> floorDTOs) {
        if (versionTag == null || versionTag.isEmpty()) {
            throw new IllegalArgumentException("Version tag must be provided.");
        }

        // generate RoomID and set defaults
        for (FloorDTO floor : floorDTOs) {
            for (RoomDTO room : floor.getRooms()) {
                if (room.getRoomId() == null || room.getRoomId().isEmpty()) {
                    room.setRoomId(generateRoomId(floor, room));
                }
                room.setIsAvailable(true);
                room.setBookingWeightage(0);
            }
        }

        FloorPlanHistory historyEntry = new FloorPlanHistory();
        historyEntry.setUsername(username);
        historyEntry.setVersionTag(versionTag);
        historyEntry.setFloorPlanData(mapToJSON(floorDTOs));
        historyEntry.setTimestamp(LocalDateTime.now());

        floorPlanRepository.save(historyEntry);
    }

    private String generateRoomId(FloorDTO floor, RoomDTO room) {
        return "FL-" + floor.getFloorNumber() + "-RM-" + System.nanoTime();
    }

    //get floor plan by versionTag
    public List<FloorDTO> getFloorPlansByVersion(String versionTag) {
        Optional<FloorPlanHistory> historyEntry =
                floorPlanRepository.findFirstByVersionTagOrderByTimestampDesc(versionTag);

        return historyEntry.map(entry -> mapToDTO(entry.getFloorPlanData()))
                .orElse(null); // Return null (or empty list) if version tag not found
    }

    // recommend room based on versionTag
    public List<RoomDTO> getRecommendedRooms(String versionTag, int participants, String lastRoomName) {
        List<FloorDTO> floorPlans = getFloorPlansByVersion(versionTag);
        if (floorPlans == null) {
            return new ArrayList<>();
        }

        List<RoomDTO> recommendedRooms = new ArrayList<>();
        for (FloorDTO floor : floorPlans) {
            for (RoomDTO room : floor.getRooms()) {
                if (Boolean.TRUE.equals(room.getIsAvailable()) && room.getCapacity() >= participants) {
                    recommendedRooms.add(room);
                }
            }
        }

        return recommendedRooms.stream()
                .sorted(Comparator.comparingInt((RoomDTO r) -> r.getRoomId().equals(lastRoomName) ? 0 : 1)
                        .thenComparingInt(RoomDTO::getBookingWeightage).reversed())
                .collect(Collectors.toList());
    }

    // book a room : this fetches the latest state, modifies it and saves it as a new entry
    public String bookRoom(String roomName, int participants, String versionTag) {
        try {
            Optional<FloorPlanHistory> latestEntryOpt =
                    floorPlanRepository.findFirstByVersionTagOrderByTimestampDesc(versionTag);

            if (!latestEntryOpt.isPresent()) {
                return "Error: Floor plan version '" + versionTag + "' not found.";
            }

            FloorPlanHistory latestEntry = latestEntryOpt.get();
            List<FloorDTO> floorPlans = mapToDTO(latestEntry.getFloorPlanData());
            boolean roomFound = false;

            for (FloorDTO floor : floorPlans) {
                for (RoomDTO room : floor.getRooms()) {
                    if (room.getName() != null && room.getName().equals(roomName)) {
                        roomFound = true;
                        if (Boolean.TRUE.equals(room.getIsAvailable()) && room.getCapacity() >= participants) {
                            room.setIsAvailable(false);
                            room.setBookingWeightage(room.getBookingWeightage() + 1);

                            FloorPlanHistory newBookingEntry = new FloorPlanHistory();
                            newBookingEntry.setUsername(latestEntry.getUsername()); //keep the same user
                            newBookingEntry.setVersionTag(versionTag); //keep the same tag
                            newBookingEntry.setTimestamp(LocalDateTime.now());
                            newBookingEntry.setFloorPlanData(mapToJSON(floorPlans));

                            floorPlanRepository.save(newBookingEntry);
                            
                            return "Room " + room.getName() + " booked successfully.";
                        } else if (!Boolean.TRUE.equals(room.getIsAvailable())) {
                            return "Room " + room.getName() + " is already booked.";
                        } else {
                            return "Room " + room.getName() + " does not have enough capacity.";
                        }
                    }
                }
            }
            return "Room with ID " + roomName + " not found in version " + versionTag;

        } catch (Exception e) {
            e.printStackTrace();
            return "An error occurred while trying to book the room.";
        }
    }

    private String mapToJSON(List<FloorDTO> floorDTOs) {
        try {
            return objectMapper.writeValueAsString(floorDTOs);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize floor plan data", e);
        }
    }

    private List<FloorDTO> mapToDTO(String json) {
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, FloorDTO.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize floor plan data", e);
        }
    }
}