package com.movieticketbooking.request.gerne;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class UpdateGenreRequest {
    private short id;
    @NotNull
    @Length(min=1, max=100)
    private String name;
}
