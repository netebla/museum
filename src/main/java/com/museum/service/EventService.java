package com.museum.service;

import com.museum.model.Event;
import com.museum.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EventService {
    private final EventRepository repo;

    public EventService(EventRepository repo) {
        this.repo = repo;
    }

    public List<Event> findAll() { return repo.findAll(); }
    public List<Event> findByTitle(String q) { return repo.findByTitleContainingIgnoreCase(q); }
    public Optional<Event> findById(Long id) { return repo.findById(id); }
    public Event save(Event e) { return repo.save(e); }
    public void deleteById(Long id) { repo.deleteById(id); }
}
