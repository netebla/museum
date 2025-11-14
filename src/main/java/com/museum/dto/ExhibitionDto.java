package com.museum.dto;

import com.museum.model.ExhibitionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ExhibitionDto {
    @NotBlank
    @Size(max = 150)
    public String title;
    public String description;
    @Size(max = 100)
    public String hall;
    public ExhibitionStatus status;
    @Size(max = 255)
    public String imageUrl;
}

