package com.museum.repository;

import com.museum.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    long countByEventId(Long eventId);
}

