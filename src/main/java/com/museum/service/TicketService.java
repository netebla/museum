package com.museum.service;

import com.museum.model.Event;
import com.museum.model.Ticket;
import com.museum.repository.EventRepository;
import com.museum.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;

    public TicketService(TicketRepository ticketRepository, EventRepository eventRepository) {
        this.ticketRepository = ticketRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public Ticket purchase(Long eventId, String buyerName, String buyerEmail) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));
        Integer total = event.getTicketsTotal() == null ? 0 : event.getTicketsTotal();
        Integer sold = event.getTicketsSold() == null ? 0 : event.getTicketsSold();
        if (total <= sold) {
            throw new IllegalStateException("Нет доступных билетов на мероприятие");
        }
        event.setTicketsSold(sold + 1);
        eventRepository.save(event);

        Ticket t = new Ticket();
        t.setEvent(event);
        t.setBuyerName(buyerName);
        t.setBuyerEmail(buyerEmail);
        t.setPurchaseDate(OffsetDateTime.now());
        return ticketRepository.save(t);
    }
}

