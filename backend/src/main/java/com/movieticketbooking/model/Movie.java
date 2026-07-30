package com.movieticketbooking.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "movies")
public class Movie {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(nullable = false)
	private String name;
	private String directorName;
	private String actorsName;
	private String description;
	private int duration;
	private String country;
	private String imageUrl;
	@ManyToMany
	@JoinTable(
			name = "movie_genres",
			joinColumns = @JoinColumn(name = "movie_id"),
			inverseJoinColumns = @JoinColumn(name = "genre_id")
	)
	private List<Genre> genres;
	private MovieStatus movieStatus;
	private LocalDateTime startAt;
	private LocalDateTime createdAt;
}