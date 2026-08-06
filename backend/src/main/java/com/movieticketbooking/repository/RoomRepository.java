package com.movieticketbooking.repository;

import com.movieticketbooking.model.Cinema;
import com.movieticketbooking.model.Room;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Integer>{
    List<Room> findByCinema(Cinema cinema);
}
