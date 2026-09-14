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
    @GetMapping("/cinema/{id}")
    public ResponseEntity<ApiResponse> getByCinemaId(@PathVariable int id)
    {
        List<RoomResponse> rooms = roomService.findByCinemaId(id);
        return ResponseEntity.ok(new ApiResponse("Get room success", rooms));
    }

    // Create new room
    @PostMapping("/add")
    public ResponseEntity<ApiResponse> createRoom(@RequestBody @Valid CreateRoomRequest request)
    {
        RoomResponse roomResponse = roomService.createRoom(request);
            return ResponseEntity.ok(new ApiResponse("Create room success", roomResponse));
    }

    // Update room (only name and description)
    @PutMapping("/edit")
    public ResponseEntity<ApiResponse> updateRoom(@RequestBody @Valid UpdateRoomRequest request)
    {
        RoomResponse roomResponse = roomService.updateRoom(request);
            return ResponseEntity.ok(new ApiResponse("Update room success", roomResponse));
    }
}
