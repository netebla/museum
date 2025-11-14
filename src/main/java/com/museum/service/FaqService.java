package com.museum.service;

import com.museum.model.Faq;
import com.museum.repository.FaqRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FaqService {
    private final FaqRepository repo;
    public FaqService(FaqRepository repo) { this.repo = repo; }
    public List<Faq> findAll() { return repo.findAll(); }
    public Optional<Faq> findById(Long id) { return repo.findById(id); }
    public Faq save(Faq f) { return repo.save(f); }
    public void deleteById(Long id) { repo.deleteById(id); }
}

