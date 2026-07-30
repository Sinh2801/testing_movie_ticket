package com.movieticketbooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BaseRepository<E, ID>  extends JpaRepository<E,ID> {
    E insert(E e);
    E update(E e);
    void delete(E e);

    @Override
    Optional<E> findById(ID id);
}
