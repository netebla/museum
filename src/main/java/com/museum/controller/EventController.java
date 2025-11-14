package com.museum.controller;

import com.museum.dto.EventDto;
import com.museum.model.Event;
import com.museum.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventService service;
    public EventController(EventService service) { this.service = service; }

    @GetMapping
    public List<Event> all() { return service.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Event> one(@PathVariable Long id) {
        return service.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Event> create(@Valid @RequestBody EventDto dto) {
        Event e = toEntity(new Event(), dto);
        Event saved = service.save(e);
        return ResponseEntity.created(URI.create("/api/events/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Event> update(@PathVariable Long id, @Valid @RequestBody EventDto dto) {
        return service.findById(id)
                .map(existing -> ResponseEntity.ok(service.save(toEntity(existing, dto))))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private Event toEntity(Event e, EventDto dto) {
        e.setTitle(dto.title);
        e.setDescription(dto.description);
        e.setStartDate(dto.startDate);
        e.setEndDate(dto.endDate);
        e.setHall(dto.hall);
        e.setTicketPrice(dto.ticketPrice);
        e.setTicketsTotal(dto.ticketsTotal);
        e.setTicketsSold(dto.ticketsSold);
        e.setImageUrl(dto.imageUrl);
        return e;
    }
}

