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

import java.util.List;

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
        List<Ticket> tickets = ticketRepository.findByUserIdOrderByPurchaseDateDesc(user.getId());
        model.addAttribute("user", user);
        model.addAttribute("tickets", tickets);
        return "profile";
    }
}

