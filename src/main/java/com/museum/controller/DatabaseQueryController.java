package com.museum.controller;

import com.museum.model.*;
import com.museum.repository.*;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/database-query")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class DatabaseQueryController {
    
    private final EventRepository eventRepository;
    private final ExhibitionRepository exhibitionRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final FaqRepository faqRepository;

    public DatabaseQueryController(
            EventRepository eventRepository,
            ExhibitionRepository exhibitionRepository,
            TicketRepository ticketRepository,
            UserRepository userRepository,
            FaqRepository faqRepository) {
        this.eventRepository = eventRepository;
        this.exhibitionRepository = exhibitionRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.faqRepository = faqRepository;
    }

    @GetMapping
    public String index() {
        return "admin/database-query";
    }

    @GetMapping("/data")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getData(
            @RequestParam("table") String table,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "50") int size,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir,
            @RequestParam Map<String, String> allParams) {
        
        try {
            Map<String, Object> result = new HashMap<>();
            
            switch (table.toLowerCase()) {
                case "events":
                    result = getEventsData(page, size, sortBy, sortDir, allParams);
                    break;
                case "exhibitions":
                    result = getExhibitionsData(page, size, sortBy, sortDir, allParams);
                    break;
                case "tickets":
                    result = getTicketsData(page, size, sortBy, sortDir, allParams);
                    break;
                case "users":
                    result = getUsersData(page, size, sortBy, sortDir, allParams);
                    break;
                case "faq":
                    result = getFaqData(page, size, sortBy, sortDir, allParams);
                    break;
                default:
                    result.put("error", "Неизвестная таблица: " + table);
                    return ResponseEntity.badRequest().body(result);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Ошибка при выполнении запроса: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    private Map<String, Object> getEventsData(int page, int size, String sortBy, String sortDir, Map<String, String> filters) {
        Specification<Event> spec = buildEventSpecification(filters);
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        
        var pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        var pageResult = eventRepository.findAll(spec, pageable);
        
        List<Map<String, Object>> data = pageResult.getContent().stream()
                .map(e -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", e.getId());
                    row.put("title", e.getTitle());
                    row.put("description", e.getDescription());
                    row.put("startDate", e.getStartDate() != null ? e.getStartDate().toString() : null);
                    row.put("endDate", e.getEndDate() != null ? e.getEndDate().toString() : null);
                    row.put("hall", e.getHall());
                    row.put("ticketPrice", e.getTicketPrice());
                    row.put("ticketsTotal", e.getTicketsTotal());
                    row.put("ticketsSold", e.getTicketsSold());
                    row.put("imageUrl", e.getImageUrl());
                    return row;
                })
                .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", data);
        result.put("total", pageResult.getTotalElements());
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", pageResult.getTotalPages());
        return result;
    }

    private Specification<Event> buildEventSpecification(Map<String, String> filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (filters.containsKey("title") && !filters.get("title").isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("title")), 
                    "%" + filters.get("title").toLowerCase() + "%"));
            }
            if (filters.containsKey("hall") && !filters.get("hall").isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("hall")), 
                    "%" + filters.get("hall").toLowerCase() + "%"));
            }
            if (filters.containsKey("startDateFrom") && !filters.get("startDateFrom").isEmpty()) {
                try {
                    LocalDate date = LocalDate.parse(filters.get("startDateFrom"));
                    predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), date));
                } catch (DateTimeParseException ignored) {}
            }
            if (filters.containsKey("startDateTo") && !filters.get("startDateTo").isEmpty()) {
                try {
                    LocalDate date = LocalDate.parse(filters.get("startDateTo"));
                    predicates.add(cb.lessThanOrEqualTo(root.get("startDate"), date));
                } catch (DateTimeParseException ignored) {}
            }
            if (filters.containsKey("ticketPriceMin") && !filters.get("ticketPriceMin").isEmpty()) {
                try {
                    BigDecimal price = new BigDecimal(filters.get("ticketPriceMin"));
                    predicates.add(cb.greaterThanOrEqualTo(root.get("ticketPrice"), price));
                } catch (NumberFormatException ignored) {}
            }
            if (filters.containsKey("ticketPriceMax") && !filters.get("ticketPriceMax").isEmpty()) {
                try {
                    BigDecimal price = new BigDecimal(filters.get("ticketPriceMax"));
                    predicates.add(cb.lessThanOrEqualTo(root.get("ticketPrice"), price));
                } catch (NumberFormatException ignored) {}
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Map<String, Object> getExhibitionsData(int page, int size, String sortBy, String sortDir, Map<String, String> filters) {
        Specification<Exhibition> spec = buildExhibitionSpecification(filters);
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        
        var pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        var pageResult = exhibitionRepository.findAll(spec, pageable);
        
        List<Map<String, Object>> data = pageResult.getContent().stream()
                .map(e -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", e.getId());
                    row.put("title", e.getTitle());
                    row.put("description", e.getDescription());
                    row.put("hall", e.getHall());
                    row.put("status", e.getStatus() != null ? e.getStatus().name() : null);
                    row.put("imageUrl", e.getImageUrl());
                    return row;
                })
                .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", data);
        result.put("total", pageResult.getTotalElements());
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", pageResult.getTotalPages());
        return result;
    }

    private Specification<Exhibition> buildExhibitionSpecification(Map<String, String> filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (filters.containsKey("title") && !filters.get("title").isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("title")), 
                    "%" + filters.get("title").toLowerCase() + "%"));
            }
            if (filters.containsKey("hall") && !filters.get("hall").isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("hall")), 
                    "%" + filters.get("hall").toLowerCase() + "%"));
            }
            if (filters.containsKey("status") && !filters.get("status").isEmpty()) {
                try {
                    ExhibitionStatus status = ExhibitionStatus.valueOf(filters.get("status"));
                    predicates.add(cb.equal(root.get("status"), status));
                } catch (IllegalArgumentException ignored) {}
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Map<String, Object> getTicketsData(int page, int size, String sortBy, String sortDir, Map<String, String> filters) {
        Specification<Ticket> spec = buildTicketSpecification(filters);
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        
        var pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        var pageResult = ticketRepository.findAll(spec, pageable);
        
        List<Map<String, Object>> data = pageResult.getContent().stream()
                .map(t -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", t.getId());
                    row.put("eventId", t.getEvent() != null ? t.getEvent().getId() : null);
                    row.put("eventTitle", t.getEvent() != null ? t.getEvent().getTitle() : null);
                    row.put("userId", t.getUser() != null ? t.getUser().getId() : null);
                    row.put("buyerName", t.getBuyerName());
                    row.put("buyerEmail", t.getBuyerEmail());
                    row.put("status", t.getStatus() != null ? t.getStatus().name() : null);
                    row.put("purchaseDate", t.getPurchaseDate() != null ? t.getPurchaseDate().toString() : null);
                    return row;
                })
                .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", data);
        result.put("total", pageResult.getTotalElements());
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", pageResult.getTotalPages());
        return result;
    }

    private Specification<Ticket> buildTicketSpecification(Map<String, String> filters) {
        return (root, query, cb) -> {
            // Загружаем связанные сущности через fetch join
            if (!query.getResultType().equals(Long.class)) {
                root.fetch("event", jakarta.persistence.criteria.JoinType.LEFT);
                root.fetch("user", jakarta.persistence.criteria.JoinType.LEFT);
            }
            
            List<Predicate> predicates = new ArrayList<>();
            
            if (filters.containsKey("buyerName") && !filters.get("buyerName").isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("buyerName")), 
                    "%" + filters.get("buyerName").toLowerCase() + "%"));
            }
            if (filters.containsKey("buyerEmail") && !filters.get("buyerEmail").isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("buyerEmail")), 
                    "%" + filters.get("buyerEmail").toLowerCase() + "%"));
            }
            if (filters.containsKey("status") && !filters.get("status").isEmpty()) {
                try {
                    TicketStatus status = TicketStatus.valueOf(filters.get("status"));
                    predicates.add(cb.equal(root.get("status"), status));
                } catch (IllegalArgumentException ignored) {}
            }
            if (filters.containsKey("eventId") && !filters.get("eventId").isEmpty()) {
                try {
                    Long eventId = Long.parseLong(filters.get("eventId"));
                    predicates.add(cb.equal(root.get("event").get("id"), eventId));
                } catch (NumberFormatException ignored) {}
            }
            if (filters.containsKey("purchaseDateFrom") && !filters.get("purchaseDateFrom").isEmpty()) {
                try {
                    // Парсим LocalDate и конвертируем в OffsetDateTime (начало дня)
                    LocalDate localDate = LocalDate.parse(filters.get("purchaseDateFrom"));
                    OffsetDateTime date = localDate.atStartOfDay().atOffset(java.time.ZoneOffset.UTC);
                    predicates.add(cb.greaterThanOrEqualTo(root.get("purchaseDate"), date));
                } catch (DateTimeParseException ignored) {}
            }
            if (filters.containsKey("purchaseDateTo") && !filters.get("purchaseDateTo").isEmpty()) {
                try {
                    // Парсим LocalDate и конвертируем в OffsetDateTime (конец дня)
                    LocalDate localDate = LocalDate.parse(filters.get("purchaseDateTo"));
                    OffsetDateTime date = localDate.atTime(23, 59, 59).atOffset(java.time.ZoneOffset.UTC);
                    predicates.add(cb.lessThanOrEqualTo(root.get("purchaseDate"), date));
                } catch (DateTimeParseException ignored) {}
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Map<String, Object> getUsersData(int page, int size, String sortBy, String sortDir, Map<String, String> filters) {
        Specification<User> spec = buildUserSpecification(filters);
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        
        var pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        var pageResult = userRepository.findAll(spec, pageable);
        
        List<Map<String, Object>> data = pageResult.getContent().stream()
                .map(u -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", u.getId());
                    row.put("username", u.getUsername());
                    row.put("email", u.getEmail());
                    row.put("role", u.getRole() != null ? u.getRole().name() : null);
                    row.put("fullName", u.getFullName());
                    row.put("passwordHash", "***"); // Не показываем пароль
                    return row;
                })
                .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", data);
        result.put("total", pageResult.getTotalElements());
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", pageResult.getTotalPages());
        return result;
    }

    private Specification<User> buildUserSpecification(Map<String, String> filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (filters.containsKey("username") && !filters.get("username").isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("username")), 
                    "%" + filters.get("username").toLowerCase() + "%"));
            }
            if (filters.containsKey("email") && !filters.get("email").isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("email")), 
                    "%" + filters.get("email").toLowerCase() + "%"));
            }
            if (filters.containsKey("role") && !filters.get("role").isEmpty()) {
                try {
                    UserRole role = UserRole.valueOf(filters.get("role"));
                    predicates.add(cb.equal(root.get("role"), role));
                } catch (IllegalArgumentException ignored) {}
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Map<String, Object> getFaqData(int page, int size, String sortBy, String sortDir, Map<String, String> filters) {
        Specification<Faq> spec = buildFaqSpecification(filters);
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        
        var pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        var pageResult = faqRepository.findAll(spec, pageable);
        
        List<Map<String, Object>> data = pageResult.getContent().stream()
                .map(f -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", f.getId());
                    row.put("question", f.getQuestion());
                    row.put("answer", f.getAnswer());
                    row.put("category", f.getCategory());
                    return row;
                })
                .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", data);
        result.put("total", pageResult.getTotalElements());
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", pageResult.getTotalPages());
        return result;
    }

    private Specification<Faq> buildFaqSpecification(Map<String, String> filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (filters.containsKey("question") && !filters.get("question").isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("question")), 
                    "%" + filters.get("question").toLowerCase() + "%"));
            }
            if (filters.containsKey("category") && !filters.get("category").isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("category")), 
                    "%" + filters.get("category").toLowerCase() + "%"));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

