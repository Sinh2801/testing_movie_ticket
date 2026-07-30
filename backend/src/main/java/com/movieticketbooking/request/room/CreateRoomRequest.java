package com.movieticketbooking.request.room;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class CreateRoomRequest {
    @Length(min = 1, max = 100)
    @NotBlank
    private String name;
    @Length(min = 1, max = 50)
    private int numberOfColumns;
    @Length(min = 1, max = 26)
    private int numberOfRows;
    private String description;
    @Length(min = 1)
    private int cinemaId;
}
