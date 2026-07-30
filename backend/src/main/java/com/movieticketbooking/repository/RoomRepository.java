package com.movieticketbooking.repository;

import com.movieticketbooking.model.Cinema;
import com.movieticketbooking.model.Room;

import java.util.List;

public interface RoomRepository extends BaseRepository<Room, Integer>{
    List<Room> findByCinema(Cinema cinema);
}
