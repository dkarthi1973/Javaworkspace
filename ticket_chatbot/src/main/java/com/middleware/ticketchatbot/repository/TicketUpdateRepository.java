package com.middleware.ticketchatbot.repository;

import com.middleware.ticketchatbot.entity.TicketUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketUpdateRepository extends JpaRepository<TicketUpdate, Long> {
    List<TicketUpdate> findByTicketId(Long ticketId);
    List<TicketUpdate> findByAuthor(String author);
}
