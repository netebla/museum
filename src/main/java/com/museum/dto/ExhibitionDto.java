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

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getHall() { return hall; }
    public void setHall(String hall) { this.hall = hall; }

    public ExhibitionStatus getStatus() { return status; }
    public void setStatus(ExhibitionStatus status) { this.status = status; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
