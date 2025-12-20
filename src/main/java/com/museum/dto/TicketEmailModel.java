package com.museum.dto;

public class TicketEmailModel {
    private String eventTitle;
    private String dateRange;
    private String hall;
    private String statusText;
    private Long ticketId;
    private String eventUrl;
    private String imageUrl;
    private int year;

    public TicketEmailModel() {
    }

    public TicketEmailModel(String eventTitle, String dateRange, String hall, String statusText,
                           Long ticketId, String eventUrl, String imageUrl, int year) {
        this.eventTitle = eventTitle;
        this.dateRange = dateRange;
        this.hall = hall;
        this.statusText = statusText;
        this.ticketId = ticketId;
        this.eventUrl = eventUrl;
        this.imageUrl = imageUrl;
        this.year = year;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getDateRange() {
        return dateRange;
    }

    public void setDateRange(String dateRange) {
        this.dateRange = dateRange;
    }

    public String getHall() {
        return hall;
    }

    public void setHall(String hall) {
        this.hall = hall;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public String getEventUrl() {
        return eventUrl;
    }

    public void setEventUrl(String eventUrl) {
        this.eventUrl = eventUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}

