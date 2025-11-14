package com.museum.controller;

import com.museum.dto.FaqDto;
import com.museum.model.Faq;
import com.museum.service.FaqService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/faq")
public class FaqController {
    private final FaqService service;
    public FaqController(FaqService service) { this.service = service; }

    @GetMapping
    public List<Faq> all() { return service.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Faq> one(@PathVariable Long id) {
        return service.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Faq> create(@Valid @RequestBody FaqDto dto) {
        Faq f = toEntity(new Faq(), dto);
        Faq saved = service.save(f);
        return ResponseEntity.created(URI.create("/api/faq/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Faq> update(@PathVariable Long id, @Valid @RequestBody FaqDto dto) {
        return service.findById(id)
                .map(existing -> ResponseEntity.ok(service.save(toEntity(existing, dto))))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private Faq toEntity(Faq f, FaqDto dto) {
        f.setQuestion(dto.question);
        f.setAnswer(dto.answer);
        f.setCategory(dto.category);
        return f;
    }
}

