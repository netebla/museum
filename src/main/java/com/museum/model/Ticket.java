package com.museum.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

@Entity
@Table(name = "tickets")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @NotBlank
    @Size(max = 100)
    @Column(name = "buyer_name")
    private String buyerName;

    @Email
    @Size(max = 100)
    @Column(name = "buyer_email")
    private String buyerEmail;

    @Enumerated(EnumType.STRING)
    private TicketStatus status = TicketStatus.SOLD;

    @Column(name = "purchase_date")
    private OffsetDateTime purchaseDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }
    public String getBuyerEmail() { return buyerEmail; }
    public void setBuyerEmail(String buyerEmail) { this.buyerEmail = buyerEmail; }
    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }
    public OffsetDateTime getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(OffsetDateTime purchaseDate) { this.purchaseDate = purchaseDate; }
}

