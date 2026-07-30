package com.movieticketbooking.request.gerne;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class UpdateGenreRequest {
    @Size(min=1)
    private short id;
    @NotNull
    @Length(min=1, max=100)
    private String name;
}
