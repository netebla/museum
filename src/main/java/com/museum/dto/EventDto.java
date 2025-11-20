package com.museum.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class EventDto {
    public Long id;

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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getHall() { return hall; }
    public void setHall(String hall) { this.hall = hall; }

    public BigDecimal getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(BigDecimal ticketPrice) { this.ticketPrice = ticketPrice; }

    public Integer getTicketsTotal() { return ticketsTotal; }
    public void setTicketsTotal(Integer ticketsTotal) { this.ticketsTotal = ticketsTotal; }

    public Integer getTicketsSold() { return ticketsSold; }
    public void setTicketsSold(Integer ticketsSold) { this.ticketsSold = ticketsSold; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
