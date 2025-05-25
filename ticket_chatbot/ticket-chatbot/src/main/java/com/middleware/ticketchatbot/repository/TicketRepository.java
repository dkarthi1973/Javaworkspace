package com.middleware.ticketchatbot.repository;

import com.middleware.ticketchatbot.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findByTicketNumber(String ticketNumber);
    List<Ticket> findByStatus(String status);
    List<Ticket> findByPriority(String priority);
    List<Ticket> findByAssignee(String assignee);

    @Query("SELECT t FROM Ticket t WHERE t.product.name = :productName")
    List<Ticket> findByProductName(@Param("productName") String productName);

    @Query("SELECT t FROM Ticket t WHERE " +
            "LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.resolution) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Ticket> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT t FROM Ticket t JOIN t.updates u WHERE " +
            "LOWER(u.comment) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Ticket> searchByUpdateComment(@Param("keyword") String keyword);
}