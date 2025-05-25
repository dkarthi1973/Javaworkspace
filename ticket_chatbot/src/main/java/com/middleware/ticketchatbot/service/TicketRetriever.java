// TicketRetriever.java
package com.middleware.ticketchatbot.service;

import com.middleware.ticketchatbot.entity.Ticket;
import com.middleware.ticketchatbot.entity.TicketUpdate;
import com.middleware.ticketchatbot.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketRetriever {

    private final TicketRepository ticketRepository;

    public List<String> retrieveRelevantTicketData(String query) {
        List<String> relevantDocs = new ArrayList<>();
        Set<Long> processedTicketIds = new HashSet<>();

        // Break query into keywords
        String[] keywords = query.toLowerCase().split("\\s+");

        // Search for each keyword
        for (String keyword : keywords) {
            if (keyword.length() < 3) continue; // Skip very short words

            // Search in title, description, resolution
            List<Ticket> tickets = ticketRepository.searchByKeyword(keyword);

            // Search in updates/comments
            tickets.addAll(ticketRepository.searchByUpdateComment(keyword));

            // Process each ticket only once
            for (Ticket ticket : tickets) {
                if (processedTicketIds.contains(ticket.getId())) {
                    continue;
                }

                processedTicketIds.add(ticket.getId());

                // Convert ticket to document format
                String ticketDoc = formatTicketAsDocument(ticket);
                relevantDocs.add(ticketDoc);
            }
        }

        // If no tickets found, check for product names
        if (relevantDocs.isEmpty()) {
            if (query.toLowerCase().contains("apache") || query.toLowerCase().contains("http")) {
                List<Ticket> tickets = ticketRepository.findByProductName("Apache HTTP Server");
                for (Ticket ticket : tickets) {
                    if (!processedTicketIds.contains(ticket.getId())) {
                        relevantDocs.add(formatTicketAsDocument(ticket));
                        processedTicketIds.add(ticket.getId());
                    }
                }
            } else if (query.toLowerCase().contains("tomcat")) {
                List<Ticket> tickets = ticketRepository.findByProductName("Apache Tomcat");
                for (Ticket ticket : tickets) {
                    if (!processedTicketIds.contains(ticket.getId())) {
                        relevantDocs.add(formatTicketAsDocument(ticket));
                        processedTicketIds.add(ticket.getId());
                    }
                }
            } else if (query.toLowerCase().contains("websphere")) {
                List<Ticket> tickets = ticketRepository.findByProductName("IBM WebSphere");
                for (Ticket ticket : tickets) {
                    if (!processedTicketIds.contains(ticket.getId())) {
                        relevantDocs.add(formatTicketAsDocument(ticket));
                        processedTicketIds.add(ticket.getId());
                    }
                }
            } else if (query.toLowerCase().contains("weblogic")) {
                List<Ticket> tickets = ticketRepository.findByProductName("Oracle WebLogic");
                for (Ticket ticket : tickets) {
                    if (!processedTicketIds.contains(ticket.getId())) {
                        relevantDocs.add(formatTicketAsDocument(ticket));
                        processedTicketIds.add(ticket.getId());
                    }
                }
            }
        }

        // Limit to top 5 most relevant tickets to avoid overwhelming the LLM
        return relevantDocs.stream().limit(5).collect(Collectors.toList());
    }

    private String formatTicketAsDocument(Ticket ticket) {
        StringBuilder doc = new StringBuilder();

        doc.append("Ticket Number: ").append(ticket.getTicketNumber()).append("\n");
        doc.append("Product: ").append(ticket.getProduct().getName()).append(" ").append(ticket.getProduct().getVersion()).append("\n");
        doc.append("Title: ").append(ticket.getTitle()).append("\n");
        doc.append("Priority: ").append(ticket.getPriority()).append("\n");
        doc.append("Status: ").append(ticket.getStatus()).append("\n");
        doc.append("Environment: ").append(ticket.getEnvironment()).append("\n");
        doc.append("Component: ").append(ticket.getComponent()).append("\n");
        doc.append("Assignee: ").append(ticket.getAssignee()).append("\n");
        doc.append("Description: ").append(ticket.getDescription()).append("\n");

        if (ticket.getResolution() != null) {
            doc.append("Resolution: ").append(ticket.getResolution()).append("\n");
        }

        // Add relevant updates/comments
        if (!ticket.getUpdates().isEmpty()) {
            doc.append("Updates and Comments:\n");
            for (TicketUpdate update : ticket.getUpdates()) {
                doc.append("- [").append(update.getAuthor()).append("]: ").append(update.getComment()).append("\n");
            }
        }

        doc.append("\n---\n\n");

        return doc.toString();
    }
}