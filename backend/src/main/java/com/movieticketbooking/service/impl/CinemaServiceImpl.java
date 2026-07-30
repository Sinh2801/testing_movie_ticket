package com.movieticketbooking.service.impl;

import com.movieticketbooking.exception.ResourceNotFoundException;
import com.movieticketbooking.model.Cinema;
import com.movieticketbooking.repository.CinemaRepository;
import com.movieticketbooking.request.cinema.CreateCinemaRequest;
import com.movieticketbooking.request.cinema.UpdateCinemaRequest;
import com.movieticketbooking.response.CinemaResponse;
import com.movieticketbooking.service.CinemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CinemaServiceImpl implements CinemaService {
    private final CinemaRepository cinemaRepository;

    // Get cinema by id
    @Override
    public Cinema findCinemaById(int id)
    {
        Optional<Cinema> opt = cinemaRepository.findById(id);
        if(opt.isEmpty()){
            throw new ResourceNotFoundException("Cinema not found");
        }
        return opt.get();
    }

    // Get cinema by id
    @Override
    public CinemaResponse getCinemaById(int id) {
        return toResponse(findCinemaById(id));
    }

    // Get all cinema
    @Override
    public List<CinemaResponse> getAllCinemas()
    {
        List<Cinema> cinemas = cinemaRepository.findAll();
        return cinemas.stream().map(this::toResponse).collect(Collectors.toList());
    }

    // Create new cinema
    @Override
    public CinemaResponse createCinema(CreateCinemaRequest request)
    {
        String name = request.getName();
        if (name == null || name.isEmpty())
            throw new IllegalArgumentException("Cinema's name is empty");

        String address = request.getAddress();
        if (address == null || address.isEmpty())
            throw new IllegalArgumentException("Cinema's address is empty");

        Cinema cinema = new Cinema();
        cinema.setName(name);
        cinema.setAddress(address);
        Cinema saved = cinemaRepository.save(cinema);
        CinemaResponse response = toResponse(saved);
        return response;
    }

    // Update cinema
    @Override
    public CinemaResponse updateCinema(UpdateCinemaRequest request)
    {
        Optional<Cinema> opt = cinemaRepository.findById(request.getId());
        if(opt.isEmpty()){
            throw new ResourceNotFoundException("Cinema not found, id: " + request.getId());
        }
        Cinema cinema = opt.get();
        if(!cinema.getName().equals(request.getName())) {
            cinema.setName(request.getName());
        }
        if(!cinema.getAddress().equals(request.getAddress())) {
            cinema.setAddress(request.getAddress());
        }
        Cinema saved = cinemaRepository.save(cinema);
        return toResponse(saved);
    }

    // map to response
    private CinemaResponse toResponse(Cinema cinema)
    {
        CinemaResponse cinemaResponse = new CinemaResponse();
        cinemaResponse.setId(cinema.getId());
        cinemaResponse.setName(cinema.getName());
        cinemaResponse.setAddress(cinema.getAddress());
        return cinemaResponse;
    }
}
