package com.movieticketbooking.service.impl;

import com.movieticketbooking.exception.ResourceNotFoundException;
import com.movieticketbooking.model.Cinema;
import com.movieticketbooking.model.Room;
import com.movieticketbooking.model.Seat;
import com.movieticketbooking.repository.RoomRepository;
import com.movieticketbooking.repository.SeatRepository;
import com.movieticketbooking.request.room.CreateRoomRequest;
import com.movieticketbooking.request.room.UpdateRoomRequest;
import com.movieticketbooking.response.RoomResponse;
import com.movieticketbooking.service.CinemaService;
import com.movieticketbooking.service.RoomService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
    private final RoomRepository roomRepository;
    private final SeatRepository seatRepository;
    private final CinemaService cinemaService;

    // get room by id (for other class)
    @Override
    public Room findRoomById(int id) {
        Optional<Room> opt = roomRepository.findById(id);
        if (opt.isEmpty())
            throw new ResourceNotFoundException("Room not found");
        return opt.get();
    }

    // Get room by id and convert to response (for web)
    @Override
    public RoomResponse getRoomById(int id) {
        return toResponse(findRoomById(id));
    }

    // Get rooms by cinema id
    @Override
    public List<RoomResponse> findByCinemaId(int cinemaId) {
        Cinema cinema = cinemaService.findCinemaById(cinemaId);
        List<Room> rooms = roomRepository.findByCinema(cinema);
        return rooms.stream().map(this::toResponse).collect(Collectors.toList());
    }

    // Create room and it's seats
    @Transactional
    @Override
    public RoomResponse createRoom(CreateRoomRequest request) {
        Cinema cinema = cinemaService.findCinemaById(request.getCinemaId());
        Room room = new Room();
        room.setCinema(cinema);
        room.setName(request.getName());
        room.setDescription(request.getDescription());
        room.setNumberOfRows(request.getNumberOfRows());
        room.setNumberOfColumns(request.getNumberOfColumns());
        Room savedRoom = roomRepository.save(room);

        List<Seat> seats = savedRoom.createSeats();
        seatRepository.saveAll(seats);
        return toResponse(savedRoom);
    }

    // Update infor of room (not update seat)
    @Override
    public RoomResponse updateRoom(UpdateRoomRequest request) {
        Optional<Room> opt = roomRepository.findById(request.getId());
        if (opt.isEmpty())
            throw new ResourceNotFoundException("Room not found");
        Room room = opt.get();
        room.setName(request.getName());
        room.setDescription(request.getDescription());
        Room savedRoom = roomRepository.save(room);
        return toResponse(savedRoom);
    }

    // map to response
    private RoomResponse toResponse(Room room) {
        RoomResponse response = new RoomResponse();
        response.setId(room.getId());
        response.setName(room.getName());
        response.setDescription(room.getDescription());
        response.setNumberOfColumns(room.getNumberOfColumns());
        response.setNumberOfRows(room.getNumberOfRows());
        return response;
    }
}
