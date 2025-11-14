package com.museum.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class EventDto {
    @NotBlank
    @Size(max = 150)
    public String title;
    public String description;
    @NotNull
    public LocalDate startDate;
    @NotNull
    public LocalDate endDate;
    @Size(max = 100)
    public String hall;
    @DecimalMin(value = "0.0", inclusive = true)
    public BigDecimal ticketPrice;
    @Min(0)
    public Integer ticketsTotal;
    @Min(0)
    public Integer ticketsSold;
    @Size(max = 255)
    public String imageUrl;
}

