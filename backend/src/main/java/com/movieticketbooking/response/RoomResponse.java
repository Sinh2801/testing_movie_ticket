package com.movieticketbooking.response;

import lombok.Data;

@Data
public class RoomResponse {
    private int id;
    private String name;
    private int numberOfColumns;
    private int numberOfRows;
    private String description;
}
