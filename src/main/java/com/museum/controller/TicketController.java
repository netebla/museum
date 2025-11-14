package com.museum.controller;

import com.museum.dto.TicketPurchaseRequest;
import com.museum.model.Ticket;
import com.museum.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/events/{eventId}/tickets")
public class TicketController {
    private final TicketService ticketService;
    public TicketController(TicketService ticketService) { this.ticketService = ticketService; }

    @PostMapping("/purchase")
    public ResponseEntity<Ticket> purchase(@PathVariable Long eventId,
                                           @Valid @RequestBody TicketPurchaseRequest req) {
        Ticket t = ticketService.purchase(eventId, req.buyerName, req.buyerEmail);
        return ResponseEntity.created(URI.create("/api/tickets/" + t.getId())).body(t);
    }
}

