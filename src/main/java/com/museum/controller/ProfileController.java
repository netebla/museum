package com.museum.controller;

import com.museum.model.Ticket;
import com.museum.model.User;
import com.museum.repository.TicketRepository;
import com.museum.repository.UserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class ProfileController {
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;

    public ProfileController(UserRepository userRepository, TicketRepository ticketRepository) {
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        
        // Ищем билеты по user_id
        List<Ticket> ticketsByUserId = ticketRepository.findByUserIdOrderByPurchaseDateDesc(user.getId());
        
        // Также ищем билеты по email, если у пользователя есть email
        List<Ticket> ticketsByEmail = new ArrayList<>();
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            ticketsByEmail = ticketRepository.findByBuyerEmailOrderByPurchaseDateDesc(user.getEmail());
        }
        
        // Объединяем списки, убирая дубликаты (по id билета)
        List<Ticket> allTickets = Stream.concat(ticketsByUserId.stream(), ticketsByEmail.stream())
                .distinct()
                .sorted((t1, t2) -> {
                    if (t1.getPurchaseDate() == null && t2.getPurchaseDate() == null) return 0;
                    if (t1.getPurchaseDate() == null) return 1;
                    if (t2.getPurchaseDate() == null) return -1;
                    return t2.getPurchaseDate().compareTo(t1.getPurchaseDate());
                })
                .collect(Collectors.toList());
        
        model.addAttribute("user", user);
        model.addAttribute("tickets", allTickets);
        return "profile";
    }
}

