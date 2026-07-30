package com.movieticketbooking.repository;

import com.movieticketbooking.model.User;

import java.util.Optional;

public interface UserRepository extends BaseRepository<User, Integer>{
    Optional<User> findByEmail(String email);
}
