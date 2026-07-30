package com.movieticketbooking.request.room;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class UpdateRoomRequest {
    @Length(min = 1)
    private int id;
    @Length(min = 1, max = 100)
    @NotBlank
    private String name;

    private String description;
}
