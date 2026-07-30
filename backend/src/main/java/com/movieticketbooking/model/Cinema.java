package com.movieticketbooking.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "cinemas")
public class Cinema {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String name;
	private String address;
	@OneToMany(mappedBy = "cinema", fetch = FetchType.LAZY,
			cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Room> rooms;
}
