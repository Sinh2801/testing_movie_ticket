package com.movieticketbooking.service.impl;

import com.movieticketbooking.exception.ResourceNotFoundException;
import com.movieticketbooking.model.*;
import com.movieticketbooking.repository.*;
import com.movieticketbooking.request.showtime.CreateShowTimeRequest;
import com.movieticketbooking.response.ShowTimeResponse;
import com.movieticketbooking.response.ShowTimeSeatResponse;
import com.movieticketbooking.service.ShowTimeService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShowTimeServiceImpl implements ShowTimeService {
    private final ShowTimeRepository showTimeRepository;
    private final CinemaRepository cinemaRepository;
    private final RoomRepository roomRepository;
    private final MovieRepository movieRepository;
    private final SeatRepository seatRepository;
    private final ShowTimeSeatRepository showTimeSeatRepository;

    @Override
    @Transactional
    public ShowTimeResponse createShowTime(CreateShowTimeRequest request) 
    {
        Cinema cinema = cinemaRepository.findById(request.getCinemaId())
                .orElseThrow(() -> new ResourceNotFoundException("Cinema not found with id: " + request.getCinemaId()));

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + request.getRoomId()));

        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + request.getMovieId()));

        ShowTime showTime = new ShowTime();
        showTime.setCinema(cinema);
        showTime.setRoom(room);
        showTime.setMovie(movie);
        showTime.setPrice(request.getPrice());
        showTime.setStartTime(request.getStartTime());
        showTime.setCreatedAt(LocalDateTime.now());

        ShowTime savedShowTime = showTimeRepository.save(showTime);

        // Fetch all seats in room and create ShowTimeSeat records
        List<Seat> roomSeats = seatRepository.findByRoomId(room.getId());
        List<ShowTimeSeat> showTimeSeats = new ArrayList<>();

        for (Seat seat : roomSeats) {
            ShowTimeSeat sts = new ShowTimeSeat();
            sts.setShowTime(savedShowTime);
            sts.setSeat(seat);
            sts.setStatus(SeatStatus.AVAILABLE);
            sts.setCreatedAt(LocalDateTime.now());
            sts.setUpdatedAt(LocalDateTime.now());
            showTimeSeats.add(sts);
        }

        showTimeSeatRepository.saveAll(showTimeSeats);

        return mapToResponse(savedShowTime);
    }

    // Get showtime by ID (for detail view)
    @Override
    public ShowTimeResponse getShowTimeById(int id) 
    {
        ShowTime showTime = showTimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ShowTime not found with id: " + id));
        return mapToResponse(showTime);
    }

    // Get showtimes by movie ID (for listing showtimes of a specific movie)
    @Override
    public List<ShowTimeResponse> getShowTimesByMovieId(int movieId) 
    {
        List<ShowTime> list = showTimeRepository.findByMovieId(movieId);
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // Get seats by showtime ID (for seat selection)
    @Override
    public List<ShowTimeSeatResponse> getSeatsByShowTimeId(int showTimeId) 
    {
        List<ShowTimeSeat> seats = showTimeSeatRepository.findByShowTimeId(showTimeId);
        return seats.stream().map(sts -> ShowTimeSeatResponse.builder()
                .id(sts.getId())
                .seatId(sts.getSeat().getId())
                .seatName(sts.getSeat().getSeatName())
                .type(sts.getSeat().getType())
                .status(sts.getStatus())
                .build()).collect(Collectors.toList());
    }

    @Override
    public List<ShowTimeResponse> filter(int page, int size) 
    {
        int pageIndex = (page > 0) ? page - 1 : 0;
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ShowTime> showTimePage = showTimeRepository.findAll(pageable);
        List<ShowTime> list = showTimePage.getContent();
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }
    
    // mapper
    private ShowTimeResponse mapToResponse(ShowTime showTime) 
    {
        return ShowTimeResponse.builder()
                .id(showTime.getId())
                .price(showTime.getPrice())
                .startTime(showTime.getStartTime())
                .createdAt(showTime.getCreatedAt())
                .cinemaId(showTime.getCinema().getId())
                .cinemaName(showTime.getCinema().getName())
                .cinemaAddress(showTime.getCinema().getAddress())
                .roomId(showTime.getRoom().getId())
                .roomName(showTime.getRoom().getName())
                .movieId(showTime.getMovie().getId())
                .movieName(showTime.getMovie().getName())
                .movieImageUrl(showTime.getMovie().getImageUrl())
                .duration(showTime.getMovie().getDuration())
                .build();
    }
}
