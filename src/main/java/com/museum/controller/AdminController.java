package com.museum.controller;

import com.museum.dto.EventDto;
import com.museum.dto.ExhibitionDto;
import com.museum.model.ExhibitionStatus;
import com.museum.service.EventService;
import com.museum.service.ExhibitionService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String index(Authentication authentication, Model model) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);
        return "admin/index";
    }

    @GetMapping("/events")
    public String events(@RequestParam(name = "q", required = false) String q,
                        @RequestParam(name = "sort", required = false, defaultValue = "title") String sort,
                        Authentication authentication,
                        Model model) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);
        var events = q != null && !q.isBlank() 
                ? eventService.findByTitle(q) 
                : eventService.findAll();
        
        // Сортировка
        events = switch (sort) {
            case "date" -> events.stream()
                    .sorted((a, b) -> {
                        if (a.getStartDate() == null && b.getStartDate() == null) return 0;
                        if (a.getStartDate() == null) return 1;
                        if (b.getStartDate() == null) return -1;
                        return a.getStartDate().compareTo(b.getStartDate());
                    })
                    .toList();
            case "dateDesc" -> events.stream()
                    .sorted((a, b) -> {
                        if (a.getStartDate() == null && b.getStartDate() == null) return 0;
                        if (a.getStartDate() == null) return 1;
                        if (b.getStartDate() == null) return -1;
                        return b.getStartDate().compareTo(a.getStartDate());
                    })
                    .toList();
            case "hall" -> events.stream()
                    .sorted((a, b) -> {
                        if (a.getHall() == null && b.getHall() == null) return 0;
                        if (a.getHall() == null) return 1;
                        if (b.getHall() == null) return -1;
                        return a.getHall().compareToIgnoreCase(b.getHall());
                    })
                    .toList();
            default -> events.stream()
                    .sorted((a, b) -> {
                        if (a.getTitle() == null && b.getTitle() == null) return 0;
                        if (a.getTitle() == null) return 1;
                        if (b.getTitle() == null) return -1;
                        return a.getTitle().compareToIgnoreCase(b.getTitle());
                    })
                    .toList();
        };
        
        if (events.isEmpty() && q != null && !q.isBlank()) {
            model.addAttribute("info", "По запросу \"" + q + "\" ничего не найдено");
        }
        
        model.addAttribute("events", events);
        model.addAttribute("query", q);
        model.addAttribute("sort", sort);
        model.addAttribute("form", new EventDto());
        return "admin/events";
    }

    @PostMapping("/events")
    public String createOrUpdateEvent(@Valid @ModelAttribute("form") EventDto form, 
                                     BindingResult br, 
                                     Authentication authentication,
                                     Model model) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);
        
        if (br.hasErrors()) {
            model.addAttribute("events", eventService.findAll());
            model.addAttribute("error", "Проверьте правильность заполнения полей");
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
    public String deleteEvent(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        if (!eventService.findById(id).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Мероприятие не найдено");
            return "redirect:/admin/events";
        }
        try {
            eventService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Мероприятие успешно удалено");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении: " + e.getMessage());
        }
        return "redirect:/admin/events";
    }

    @GetMapping("/events/{id}/edit")
    public String editEvent(@PathVariable("id") Long id, Model model) {
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
    public String exhibitions(@RequestParam(name = "q", required = false) String q,
                             @RequestParam(name = "sort", required = false, defaultValue = "title") String sort,
                             Authentication authentication,
                             Model model) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);
        var exhibitions = q != null && !q.isBlank() 
                ? exhibitionService.findByTitle(q) 
                : exhibitionService.findAll();
        
        // Сортировка
        exhibitions = switch (sort) {
            case "hall" -> exhibitions.stream()
                    .sorted((a, b) -> {
                        if (a.getHall() == null && b.getHall() == null) return 0;
                        if (a.getHall() == null) return 1;
                        if (b.getHall() == null) return -1;
                        return a.getHall().compareToIgnoreCase(b.getHall());
                    })
                    .toList();
            case "status" -> exhibitions.stream()
                    .sorted((a, b) -> {
                        if (a.getStatus() == null && b.getStatus() == null) return 0;
                        if (a.getStatus() == null) return 1;
                        if (b.getStatus() == null) return -1;
                        return a.getStatus().name().compareTo(b.getStatus().name());
                    })
                    .toList();
            default -> exhibitions.stream()
                    .sorted((a, b) -> {
                        if (a.getTitle() == null && b.getTitle() == null) return 0;
                        if (a.getTitle() == null) return 1;
                        if (b.getTitle() == null) return -1;
                        return a.getTitle().compareToIgnoreCase(b.getTitle());
                    })
                    .toList();
        };
        
        if (exhibitions.isEmpty() && q != null && !q.isBlank()) {
            model.addAttribute("info", "По запросу \"" + q + "\" ничего не найдено");
        }
        
        model.addAttribute("exhibitions", exhibitions);
        model.addAttribute("query", q);
        model.addAttribute("sort", sort);
        model.addAttribute("form", new ExhibitionDto());
        model.addAttribute("statuses", ExhibitionStatus.values());
        return "admin/exhibitions";
    }

    @PostMapping("/exhibitions")
    public String createOrUpdateExhibition(@Valid @ModelAttribute("form") ExhibitionDto form, 
                                          BindingResult br,
                                          Authentication authentication,
                                          Model model) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);
        
        if (br.hasErrors()) {
            model.addAttribute("exhibitions", exhibitionService.findAll());
            model.addAttribute("statuses", ExhibitionStatus.values());
            model.addAttribute("error", "Проверьте правильность заполнения полей");
            return "admin/exhibitions";
        }
        var ex = form.getId() != null
                ? exhibitionService.findById(form.getId()).orElseGet(com.museum.model.Exhibition::new)
                : new com.museum.model.Exhibition();
        ex.setTitle(form.title);
        ex.setDescription(form.description);
        ex.setHall(form.hall);
        ex.setStatus(form.status);
        ex.setImageUrl(form.imageUrl);
        exhibitionService.save(ex);
        return "redirect:/admin/exhibitions";
    }

    @PostMapping("/exhibitions/{id}/delete")
    public String deleteExhibition(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        if (!exhibitionService.findById(id).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Экспозиция не найдена");
            return "redirect:/admin/exhibitions";
        }
        try {
            exhibitionService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Экспозиция успешно удалена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении: " + e.getMessage());
        }
        return "redirect:/admin/exhibitions";
    }

    @GetMapping("/exhibitions/{id}/edit")
    public String editExhibition(@PathVariable("id") Long id, Model model) {
        var opt = exhibitionService.findById(id);
        if (opt.isEmpty()) {
            return "redirect:/admin/exhibitions";
        }
        var e = opt.get();
        var dto = new ExhibitionDto();
        dto.setId(e.getId());
        dto.setTitle(e.getTitle());
        dto.setDescription(e.getDescription());
        dto.setHall(e.getHall());
        dto.setStatus(e.getStatus());
        dto.setImageUrl(e.getImageUrl());

        model.addAttribute("exhibitions", exhibitionService.findAll());
        model.addAttribute("form", dto);
        model.addAttribute("statuses", ExhibitionStatus.values());
        return "admin/exhibitions";
    }
}
