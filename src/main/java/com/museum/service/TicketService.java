package com.museum.service;

import com.museum.model.Event;
import com.museum.model.Ticket;
import com.museum.model.User;
import com.museum.repository.EventRepository;
import com.museum.repository.TicketRepository;
import com.museum.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public TicketService(TicketRepository ticketRepository,
                         EventRepository eventRepository,
                         NotificationService notificationService,
                         UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.eventRepository = eventRepository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @Transactional
    public Ticket purchase(Long eventId, String buyerName, String buyerEmail, User user) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));
        Integer total = event.getTicketsTotal() == null ? 0 : event.getTicketsTotal();
        Integer sold = event.getTicketsSold() == null ? 0 : event.getTicketsSold();
        if (total <= sold) {
            throw new IllegalStateException("Нет доступных билетов на мероприятие");
        }
        event.setTicketsSold(sold + 1);
        eventRepository.save(event);

        // Если пользователь не передан, но указан email, пытаемся найти пользователя по email
        if (user == null && buyerEmail != null && !buyerEmail.isBlank()) {
            user = userRepository.findByEmail(buyerEmail).orElse(null);
        }

        Ticket t = new Ticket();
        t.setEvent(event);
        t.setUser(user);
        t.setBuyerName(buyerName);
        t.setBuyerEmail(buyerEmail);
        t.setPurchaseDate(OffsetDateTime.now());
        Ticket saved = ticketRepository.save(t);
        notificationService.sendTicketPurchaseConfirmation(saved);
        return saved;
    }
}
