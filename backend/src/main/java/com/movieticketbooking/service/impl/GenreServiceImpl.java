package com.movieticketbooking.service.impl;

import com.movieticketbooking.exception.ResourceNotFoundException;
import com.movieticketbooking.model.Genre;
import com.movieticketbooking.repository.GenreRepository;
import com.movieticketbooking.request.gerne.CreateGenreRequest;
import com.movieticketbooking.request.gerne.UpdateGenreRequest;
import com.movieticketbooking.response.GenreResponse;
import com.movieticketbooking.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {
    private final GenreRepository genreRepository;

    // Get all genres
    @Override
    public List<GenreResponse> getAllGenres()
    {
        return genreRepository.findAll()
                .stream().map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Genre findGenreById(short id) {
        Optional<Genre> opt = genreRepository.findById(id);
        if(opt.isEmpty()) throw new ResourceNotFoundException("Genre not found");
        return opt.get();
    }

    // Get by id
    @Override
    public GenreResponse getById(short id)
    {
        Optional<Genre> opt = genreRepository.findById(id);
        if(opt.isEmpty()) throw new ResourceNotFoundException("Genre not found");
        return toResponse(opt.get());
    }

    // Add new genre
    @Override
    public GenreResponse addGenre(CreateGenreRequest request)
    {
        String name = request.getName();
        Genre genre = new Genre();
        genre.setName(name);
        Genre saved = genreRepository.save(genre);
        return toResponse(saved);
    }

    // Update genre by id
    @Override
    public GenreResponse updateGenre(UpdateGenreRequest request) {
        Optional<Genre> opt = genreRepository.findById(request.getId());
        if(opt.isEmpty()) throw new ResourceNotFoundException("Genre not found");
        Genre genre = opt.get();
        genre.setName(request.getName());
        Genre saved = genreRepository.save(genre);
        return toResponse(saved);
    }

    // mapper
    private GenreResponse toResponse(Genre genre)
    {
        GenreResponse genreResponse = new GenreResponse();
        genreResponse.setId(genre.getId());
        genreResponse.setName(genre.getName());
        return genreResponse;
    }
}
