package com.movieticketbooking.controller;

import com.movieticketbooking.request.room.CreateRoomRequest;
import com.movieticketbooking.request.room.UpdateRoomRequest;
import com.movieticketbooking.response.ApiResponse;
import com.movieticketbooking.response.RoomResponse;
import com.movieticketbooking.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/rooms")
public class RoomController {
    private final RoomService roomService;

    // Get rooms by cinema id
    @GetMapping("/movie/{id}")
    public ResponseEntity<ApiResponse> getByCinemaId(@PathVariable int id)
    {
        try {
            List<RoomResponse> rooms = roomService.findByCinemaId(id);
            return ResponseEntity.ok()
                    .body(new ApiResponse("Get room success", rooms));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("Get room failed", e.getMessage()));
        }
    }

    // Create new room
    @PostMapping("/add")
    public ResponseEntity<ApiResponse> createRoom(@RequestBody @Valid CreateRoomRequest request)
    {
        try {
            RoomResponse roomResponse = roomService.createRoom(request);
            return ResponseEntity.ok()
                    .body(new ApiResponse("Create room success", roomResponse));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("Create room failed", e.getMessage()));
        }
    }

    // Update room (only name and description)
    @PutMapping("/update")
    public ResponseEntity<ApiResponse> updateRoom(@RequestBody @Valid UpdateRoomRequest request)
    {
        try {
            RoomResponse roomResponse = roomService.updateRoom(request);
            return ResponseEntity.ok()
                    .body(new ApiResponse("Update room success", roomResponse));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("Update room failed", e.getMessage()));
        }
    }
}
