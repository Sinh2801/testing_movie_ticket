package com.movieticketbooking.service;

import com.movieticketbooking.model.Room;
import com.movieticketbooking.request.room.CreateRoomRequest;
import com.movieticketbooking.request.room.UpdateRoomRequest;
import com.movieticketbooking.response.RoomResponse;

import java.util.List;

public interface RoomService {
    Room findRoomById(int id);
    RoomResponse getRoomById(int id);
    List<RoomResponse> findByCinemaId(int cinemaId);
    RoomResponse createRoom(CreateRoomRequest request);
    RoomResponse updateRoom(UpdateRoomRequest request);
}
