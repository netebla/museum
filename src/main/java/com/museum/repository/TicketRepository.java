package com.museum.repository;

import com.museum.model.Ticket;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {
    long countByEventId(Long eventId);

    @EntityGraph(attributePaths = {"event"})
    List<Ticket> findByUserIdOrderByPurchaseDateDesc(Long userId);
    
    @EntityGraph(attributePaths = {"event"})
    List<Ticket> findByBuyerEmailOrderByPurchaseDateDesc(String buyerEmail);
    
    @EntityGraph(attributePaths = {"event"})
    @Query("SELECT t FROM Ticket t")
    List<Ticket> findAllWithEvent();
}
