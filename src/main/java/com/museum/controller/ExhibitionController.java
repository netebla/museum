package com.museum.controller;

import com.museum.dto.ExhibitionDto;
import com.museum.model.Exhibition;
import com.museum.service.ExhibitionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/exhibitions")
public class ExhibitionController {
    private final ExhibitionService service;
    public ExhibitionController(ExhibitionService service) { this.service = service; }

    @GetMapping
    public List<Exhibition> all() { return service.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Exhibition> one(@PathVariable Long id) {
        return service.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Exhibition> create(@Valid @RequestBody ExhibitionDto dto) {
        Exhibition e = toEntity(new Exhibition(), dto);
        Exhibition saved = service.save(e);
        return ResponseEntity.created(URI.create("/api/exhibitions/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Exhibition> update(@PathVariable Long id, @Valid @RequestBody ExhibitionDto dto) {
        return service.findById(id)
                .map(existing -> ResponseEntity.ok(service.save(toEntity(existing, dto))))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private Exhibition toEntity(Exhibition e, ExhibitionDto dto) {
        e.setTitle(dto.title);
        e.setDescription(dto.description);
        e.setHall(dto.hall);
        e.setStatus(dto.status);
        e.setImageUrl(dto.imageUrl);
        return e;
    }
}

