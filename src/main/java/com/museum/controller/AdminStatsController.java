package com.museum.controller;

import com.museum.model.Event;
import com.museum.model.Exhibition;
import com.museum.model.ExhibitionStatus;
import com.museum.model.Ticket;
import com.museum.repository.TicketRepository;
import com.museum.repository.UserRepository;
import com.museum.service.EventService;
import com.museum.service.ExhibitionService;
import com.museum.service.NotificationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/stats")
public class AdminStatsController {
    private final EventService eventService;
    private final ExhibitionService exhibitionService;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final NotificationService notificationService;

    public AdminStatsController(EventService eventService, ExhibitionService exhibitionService,
                                UserRepository userRepository, TicketRepository ticketRepository,
                                NotificationService notificationService) {
        this.eventService = eventService;
        this.exhibitionService = exhibitionService;
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
        this.notificationService = notificationService;
    }

    @GetMapping
    public String stats(Model model) {
        List<Event> events = eventService.findAll();
        List<Exhibition> exhibitions = exhibitionService.findAll();

        LocalDate today = LocalDate.now();

        long totalEvents = events.size();
        long upcomingEvents = events.stream()
                .filter(e -> e.getEndDate() != null && !e.getEndDate().isBefore(today))
                .count();
        long pastEvents = events.stream()
                .filter(e -> e.getEndDate() != null && e.getEndDate().isBefore(today))
                .count();

        long totalExhibitions = exhibitions.size();
        Map<ExhibitionStatus, Long> exhibitionsByStatus = new EnumMap<>(ExhibitionStatus.class);
        for (ExhibitionStatus status : ExhibitionStatus.values()) {
            long count = exhibitions.stream()
                    .filter(x -> status.equals(x.getStatus()))
                    .count();
            exhibitionsByStatus.put(status, count);
        }

        List<Event> topByTickets = events.stream()
                .filter(e -> e.getTicketsSold() != null && e.getTicketsSold() > 0)
                .sorted(Comparator.comparing(Event::getTicketsSold, Comparator.nullsLast(Integer::compareTo)).reversed())
                .limit(3)
                .toList();

        long totalUsers = userRepository.count();
        long totalTickets = ticketRepository.count();
        
        // Среднее время отправки email
        double avgEmailSendTime = notificationService.getAverageEmailSendTime();
        int emailSendCount = notificationService.getEmailSendCount();
        
        // Среднее время ожидания до начала мероприятия (от покупки билета до начала события)
        List<Ticket> allTickets = ticketRepository.findAll();
        double avgWaitingDays = allTickets.stream()
                .filter(t -> t.getPurchaseDate() != null && 
                            t.getEvent() != null && 
                            t.getEvent().getStartDate() != null &&
                            t.getPurchaseDate().toLocalDate().isBefore(t.getEvent().getStartDate()))
                .mapToLong(t -> {
                    LocalDate purchaseDate = t.getPurchaseDate().toLocalDate();
                    LocalDate eventStartDate = t.getEvent().getStartDate();
                    return ChronoUnit.DAYS.between(purchaseDate, eventStartDate);
                })
                .average()
                .orElse(0.0);

        model.addAttribute("totalEvents", totalEvents);
        model.addAttribute("upcomingEvents", upcomingEvents);
        model.addAttribute("pastEvents", pastEvents);
        model.addAttribute("totalExhibitions", totalExhibitions);
        model.addAttribute("exhibitionsByStatus", exhibitionsByStatus);
        model.addAttribute("topEvents", topByTickets);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalTickets", totalTickets);
        model.addAttribute("avgEmailSendTime", Math.round(avgEmailSendTime * 10.0) / 10.0);
        model.addAttribute("emailSendCount", emailSendCount);
        model.addAttribute("avgWaitingDays", Math.round(avgWaitingDays * 10.0) / 10.0);

        return "admin/stats";
    }
}

