package com.museum.controller;

import com.museum.model.Event;
import com.museum.model.Exhibition;
import com.museum.model.ExhibitionStatus;
import com.museum.service.EventService;
import com.museum.service.ExhibitionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/stats")
public class AdminStatsController {
    private final EventService eventService;
    private final ExhibitionService exhibitionService;

    public AdminStatsController(EventService eventService, ExhibitionService exhibitionService) {
        this.eventService = eventService;
        this.exhibitionService = exhibitionService;
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

        model.addAttribute("totalEvents", totalEvents);
        model.addAttribute("upcomingEvents", upcomingEvents);
        model.addAttribute("pastEvents", pastEvents);
        model.addAttribute("totalExhibitions", totalExhibitions);
        model.addAttribute("exhibitionsByStatus", exhibitionsByStatus);
        model.addAttribute("topEvents", topByTickets);

        return "admin/stats";
    }
}

