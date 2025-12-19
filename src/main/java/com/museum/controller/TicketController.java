package com.museum.controller;

import com.museum.dto.TicketPurchaseRequest;
import com.museum.model.Ticket;
import com.museum.model.User;
import com.museum.repository.UserRepository;
import com.museum.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/events/{eventId}/tickets")
public class TicketController {
    private final TicketService ticketService;
    private final UserRepository userRepository;

    public TicketController(TicketService ticketService, UserRepository userRepository) {
        this.ticketService = ticketService;
        this.userRepository = userRepository;
    }

    @PostMapping("/purchase")
    public ResponseEntity<Ticket> purchase(@PathVariable("eventId") Long eventId,
                                           @Valid @RequestBody TicketPurchaseRequest req,
                                           Authentication authentication) {
        User user = null;
        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            user = userRepository.findByUsername(authentication.getName()).orElse(null);
            if (user != null) {
                if ((req.buyerName == null || req.buyerName.isBlank()) && user.getFullName() != null) {
                    req.buyerName = user.getFullName();
                }
                if ((req.buyerEmail == null || req.buyerEmail.isBlank()) && user.getEmail() != null) {
                    req.buyerEmail = user.getEmail();
                }
            }
        }
        Ticket t = ticketService.purchase(eventId, req.buyerName, req.buyerEmail, user);
        return ResponseEntity.created(URI.create("/api/tickets/" + t.getId())).body(t);
    }
}
