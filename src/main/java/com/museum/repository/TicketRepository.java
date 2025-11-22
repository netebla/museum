package com.museum.repository;

import com.museum.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    long countByEventId(Long eventId);

    List<Ticket> findByUserIdOrderByPurchaseDateDesc(Long userId);
}
