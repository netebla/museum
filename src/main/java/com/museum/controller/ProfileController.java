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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        // Сравниваем email без учета регистра
        List<Ticket> ticketsByEmail = new ArrayList<>();
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            String userEmail = user.getEmail().trim().toLowerCase();
            // Ищем билеты с точным совпадением email (без учета регистра)
            List<Ticket> allTicketsByEmail = ticketRepository.findByBuyerEmailOrderByPurchaseDateDesc(user.getEmail());
            // Фильтруем те, где email совпадает без учета регистра
            ticketsByEmail = allTicketsByEmail.stream()
                    .filter(t -> t.getBuyerEmail() != null 
                            && t.getBuyerEmail().trim().toLowerCase().equals(userEmail))
                    .collect(Collectors.toList());
        }
        
        // Объединяем списки, убирая дубликаты по id билета
        // Используем LinkedHashMap для сохранения порядка и удаления дубликатов
        Map<Long, Ticket> ticketsMap = new LinkedHashMap<>();
        ticketsByUserId.forEach(t -> ticketsMap.put(t.getId(), t));
        ticketsByEmail.forEach(t -> ticketsMap.put(t.getId(), t));
        
        // Сортируем по дате покупки (новые первыми)
        List<Ticket> allTickets = new ArrayList<>(ticketsMap.values());
        allTickets.sort((t1, t2) -> {
            if (t1.getPurchaseDate() == null && t2.getPurchaseDate() == null) return 0;
            if (t1.getPurchaseDate() == null) return 1;
            if (t2.getPurchaseDate() == null) return -1;
            return t2.getPurchaseDate().compareTo(t1.getPurchaseDate());
        });
        
        model.addAttribute("user", user);
        model.addAttribute("tickets", allTickets);
        return "profile";
    }
}

