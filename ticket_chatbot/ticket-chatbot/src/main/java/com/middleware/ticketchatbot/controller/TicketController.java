// TicketController.java (Updated with Swagger annotations)
package com.middleware.ticketchatbot.controller;

import com.middleware.ticketchatbot.entity.Ticket;
import com.middleware.ticketchatbot.repository.TicketRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Tickets", description = "ServiceNow ticket management and search operations")
public class TicketController {

    private final TicketRepository ticketRepository;

    @GetMapping
    @Operation(
            summary = "Get all tickets with pagination",
            description = "Retrieve all ServiceNow tickets with pagination support. Results are sorted by creation date (newest first) by default."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved tickets",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Page.class)
            )
    )
    public Page<Ticket> getAllTickets(
            @Parameter(description = "Pagination and sorting parameters")
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ticketRepository.findAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get ticket by ID",
            description = "Retrieve a specific ticket by its unique identifier"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Ticket found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Ticket.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ticket not found"
            )
    })
    public ResponseEntity<Ticket> getTicketById(
            @Parameter(description = "Unique identifier of the ticket", example = "1")
            @PathVariable Long id) {
        Optional<Ticket> ticket = ticketRepository.findById(id);
        return ticket.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search tickets by keyword",
            description = """
            Search for tickets containing the specified keyword in title, description, or resolution.
            The search is case-insensitive and matches partial text.
            
            **Example keywords:**
            - memory (finds OutOfMemoryError tickets)
            - apache (finds Apache-related issues)
            - ssl (finds certificate problems)
            - cluster (finds clustering issues)
            """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Search results",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Search Results",
                            description = "Example search results for 'memory'"
                    )
            )
    )
    public List<Ticket> searchTickets(
            @Parameter(
                    description = "Keyword to search for in ticket title, description, and resolution",
                    example = "memory",
                    required = true
            )
            @RequestParam String keyword) {
        return ticketRepository.searchByKeyword(keyword);
    }

    @GetMapping("/product/{productName}")
    @Operation(
            summary = "Get tickets by product name",
            description = "Retrieve all tickets for a specific middleware product"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Tickets for the specified product"
    )
    public List<Ticket> getTicketsByProduct(
            @Parameter(
                    description = "Name of the middleware product",
                    example = "Apache Tomcat",
                    schema = @Schema(allowableValues = {
                            "Apache HTTP Server",
                            "Apache Tomcat",
                            "IBM WebSphere",
                            "Oracle WebLogic"
                    })
            )
            @PathVariable String productName) {
        return ticketRepository.findByProductName(productName);
    }

    @GetMapping("/status/{status}")
    @Operation(
            summary = "Get tickets by status",
            description = "Filter tickets by their current status"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Tickets with the specified status"
    )
    public List<Ticket> getTicketsByStatus(
            @Parameter(
                    description = "Current status of the tickets",
                    example = "Resolved",
                    schema = @Schema(allowableValues = {
                            "Open",
                            "In Progress",
                            "Resolved",
                            "Closed",
                            "Cancelled"
                    })
            )
            @PathVariable String status) {
        return ticketRepository.findByStatus(status);
    }

    @GetMapping("/priority/{priority}")
    @Operation(
            summary = "Get tickets by priority",
            description = "Filter tickets by their priority level"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Tickets with the specified priority"
    )
    public List<Ticket> getTicketsByPriority(
            @Parameter(
                    description = "Priority level of the tickets",
                    example = "High",
                    schema = @Schema(allowableValues = {
                            "Low",
                            "Medium",
                            "High",
                            "Critical"
                    })
            )
            @PathVariable String priority) {
        return ticketRepository.findByPriority(priority);
    }

    @GetMapping("/stats")
    @Operation(
            summary = "Get ticket statistics",
            description = "Get statistical information about tickets in the database"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Ticket statistics",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Statistics",
                            value = """
                {
                  "totalTickets": 8,
                  "resolvedTickets": 4,
                  "inProgressTickets": 4,
                  "highPriorityTickets": 4,
                  "criticalTickets": 2
                }
                """
                    )
            )
    )
    public ResponseEntity<Map<String, Object>> getTicketStats() {
        long totalTickets = ticketRepository.count();
        long resolvedTickets = ticketRepository.findByStatus("Resolved").size();
        long inProgressTickets = ticketRepository.findByStatus("In Progress").size();
        long highPriorityTickets = ticketRepository.findByPriority("High").size();
        long criticalTickets = ticketRepository.findByPriority("Critical").size();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTickets", totalTickets);
        stats.put("resolvedTickets", resolvedTickets);
        stats.put("inProgressTickets", inProgressTickets);
        stats.put("highPriorityTickets", highPriorityTickets);
        stats.put("criticalTickets", criticalTickets);

        return ResponseEntity.ok(stats);
    }

}