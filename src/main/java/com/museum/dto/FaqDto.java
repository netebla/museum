package com.museum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class FaqDto {
    @NotBlank
    @Size(max = 255)
    public String question;
    public String answer;
    @Size(max = 100)
    public String category;
}

