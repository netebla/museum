package com.museum.controller;

import com.museum.dto.EventDto;
import com.museum.dto.ExhibitionDto;
import com.museum.model.ExhibitionStatus;
import com.museum.service.EventService;
import com.museum.service.ExhibitionService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final EventService eventService;
    private final ExhibitionService exhibitionService;

    public AdminController(EventService eventService, ExhibitionService exhibitionService) {
        this.eventService = eventService;
        this.exhibitionService = exhibitionService;
    }

    @GetMapping
    public String index() {
        return "admin/index";
    }

    @GetMapping("/events")
    public String events(Model model) {
        model.addAttribute("events", eventService.findAll());
        model.addAttribute("form", new EventDto());
        return "admin/events";
    }

    @PostMapping("/events")
    public String createOrUpdateEvent(@Valid @ModelAttribute("form") EventDto form, BindingResult br, Model model) {
        if (br.hasErrors()) {
            model.addAttribute("events", eventService.findAll());
            return "admin/events";
        }
        com.museum.model.Event e = form.getId() != null
                ? eventService.findById(form.getId()).orElseGet(com.museum.model.Event::new)
                : new com.museum.model.Event();
        e.setTitle(form.title);
        e.setDescription(form.description);
        e.setStartDate(form.startDate);
        e.setEndDate(form.endDate);
        e.setHall(form.hall);
        e.setTicketPrice(form.ticketPrice);
        e.setTicketsTotal(form.ticketsTotal);
        e.setTicketsSold(form.ticketsSold);
        e.setImageUrl(form.imageUrl);
        eventService.save(e);
        return "redirect:/admin/events";
    }

    @PostMapping("/events/{id}/delete")
    public String deleteEvent(@PathVariable Long id) {
        eventService.deleteById(id);
        return "redirect:/admin/events";
    }

    @GetMapping("/events/{id}/edit")
    public String editEvent(@PathVariable Long id, Model model) {
        var opt = eventService.findById(id);
        if (opt.isEmpty()) {
            return "redirect:/admin/events";
        }
        var e = opt.get();
        var dto = new EventDto();
        dto.setId(e.getId());
        dto.setTitle(e.getTitle());
        dto.setDescription(e.getDescription());
        dto.setStartDate(e.getStartDate());
        dto.setEndDate(e.getEndDate());
        dto.setHall(e.getHall());
        dto.setTicketPrice(e.getTicketPrice());
        dto.setTicketsTotal(e.getTicketsTotal());
        dto.setTicketsSold(e.getTicketsSold());
        dto.setImageUrl(e.getImageUrl());

        model.addAttribute("events", eventService.findAll());
        model.addAttribute("form", dto);
        return "admin/events";
    }

    @GetMapping("/exhibitions")
    public String exhibitions(Model model) {
        model.addAttribute("exhibitions", exhibitionService.findAll());
        model.addAttribute("form", new ExhibitionDto());
        model.addAttribute("statuses", ExhibitionStatus.values());
        return "admin/exhibitions";
    }

    @PostMapping("/exhibitions")
    public String createExhibition(@Valid @ModelAttribute("form") ExhibitionDto form, BindingResult br, Model model) {
        if (br.hasErrors()) {
            model.addAttribute("exhibitions", exhibitionService.findAll());
            model.addAttribute("statuses", ExhibitionStatus.values());
            return "admin/exhibitions";
        }
        var ex = new com.museum.model.Exhibition();
        ex.setTitle(form.title);
        ex.setDescription(form.description);
        ex.setHall(form.hall);
        ex.setStatus(form.status);
        ex.setImageUrl(form.imageUrl);
        exhibitionService.save(ex);
        return "redirect:/admin/exhibitions";
    }

    @PostMapping("/exhibitions/{id}/delete")
    public String deleteExhibition(@PathVariable Long id) {
        exhibitionService.deleteById(id);
        return "redirect:/admin/exhibitions";
    }
}
