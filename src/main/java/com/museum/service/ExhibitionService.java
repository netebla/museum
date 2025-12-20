package com.museum.service;

import com.museum.model.Exhibition;
import com.museum.repository.ExhibitionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExhibitionService {
    private final ExhibitionRepository repo;
    public ExhibitionService(ExhibitionRepository repo) { this.repo = repo; }
    public List<Exhibition> findAll() { return repo.findAll(); }
    public List<Exhibition> findByTitle(String q) { return repo.findByTitleContainingIgnoreCase(q); }
    public Optional<Exhibition> findById(Long id) { return repo.findById(id); }
    public Exhibition save(Exhibition e) { return repo.save(e); }
    public void deleteById(Long id) { repo.deleteById(id); }
}

