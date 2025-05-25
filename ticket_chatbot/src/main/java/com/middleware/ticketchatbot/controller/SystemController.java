// SystemController.java
package com.middleware.ticketchatbot.controller;

import com.middleware.ticketchatbot.repository.ProductRepository;
import com.middleware.ticketchatbot.repository.TicketRepository;
import com.middleware.ticketchatbot.repository.TicketUpdateRepository;
import dev.langchain4j.model.chat.ChatLanguageModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Health", description = "System health monitoring and status information")
public class SystemController {

    private final ProductRepository productRepository;
    private final TicketRepository ticketRepository;
    private final TicketUpdateRepository ticketUpdateRepository;
    private final ChatLanguageModel chatLanguageModel;

    @Value("${ollama.base-url}")
    private String ollamaBaseUrl;

    @Value("${ollama.model}")
    private String ollamaModel;

    @GetMapping("/health")
    @Operation(
            summary = "Complete system health check",
            description = """
            Performs a comprehensive health check of all system components:
            - Database connectivity and data counts
            - Ollama service connectivity
            - AI model availability
            - System configuration
            """
    )
    @ApiResponse(
            responseCode = "200",
            description = "System health status",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Health Check Response",
                            value = """
                {
                  "status": "healthy",
                  "timestamp": "2024-12-19T10:30:45",
                  "database": {
                    "status": "connected",
                    "productCount": 4,
                    "ticketCount": 8,
                    "updateCount": 32
                  },
                  "aiModel": {
                    "status": "available",
                    "ollamaUrl": "http://localhost:11434",
                    "modelName": "llama3",
                    "testResponse": "AI model is responding correctly"
                  },
                  "configuration": {
                    "serverPort": 8080,
                    "swaggerEnabled": true,
                    "h2ConsoleEnabled": true
                  }
                }
                """
                    )
            )
    )
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();

        try {
            // Database health
            Map<String, Object> dbHealth = new HashMap<>();
            dbHealth.put("status", "connected");
            dbHealth.put("productCount", productRepository.count());
            dbHealth.put("ticketCount", ticketRepository.count());
            dbHealth.put("updateCount", ticketUpdateRepository.count());

            // AI Model health
            Map<String, Object> aiHealth = new HashMap<>();
            try {
                String testResponse = chatLanguageModel.generate("Test connection - respond with 'OK'");
                aiHealth.put("status", "available");
                aiHealth.put("ollamaUrl", ollamaBaseUrl);
                aiHealth.put("modelName", ollamaModel);
                aiHealth.put("testResponse", testResponse.trim());
            } catch (Exception e) {
                aiHealth.put("status", "unavailable");
                aiHealth.put("error", e.getMessage());
            }

            // Configuration info
            Map<String, Object> config = new HashMap<>();
            config.put("serverPort", 8080);
            config.put("swaggerEnabled", true);
            config.put("h2ConsoleEnabled", true);

            health.put("status", "healthy");
            health.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            health.put("database", dbHealth);
            health.put("aiModel", aiHealth);
            health.put("configuration", config);

            return ResponseEntity.ok(health);

        } catch (Exception e) {
            health.put("status", "unhealthy");
            health.put("error", e.getMessage());
            health.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            return ResponseEntity.status(500).body(health);
        }
    }

    @GetMapping("/info")
    @Operation(
            summary = "Get system information",
            description = "Returns general information about the application and its capabilities"
    )
    @ApiResponse(
            responseCode = "200",
            description = "System information",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "System Info",
                            value = """
                {
                  "applicationName": "Middleware Support Chatbot",
                  "version": "1.0.0",
                  "description": "AI-powered chatbot for middleware support using RAG pattern",
                  "features": [
                    "Natural language query processing",
                    "Historical ticket search and analysis",
                    "Local AI model processing",
                    "RESTful API with Swagger documentation",
                    "H2 database with sample data"
                  ],
                  "supportedProducts": [
                    "Apache HTTP Server",
                    "Apache Tomcat",
                    "IBM WebSphere", 
                    "Oracle WebLogic"
                  ],
                  "endpoints": {
                    "swagger": "/swagger-ui.html",
                    "h2Console": "/h2-console",
                    "webInterface": "/static/index.html",
                    "apiDocs": "/api-docs"
                  }
                }
                """
                    )
            )
    )
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();

        info.put("applicationName", "Middleware Support Chatbot");
        info.put("version", "1.0.0");
        info.put("description", "AI-powered chatbot for middleware support using RAG pattern");

        info.put("features", new String[]{
                "Natural language query processing",
                "Historical ticket search and analysis",
                "Local AI model processing",
                "RESTful API with Swagger documentation",
                "H2 database with sample data"
        });

        info.put("supportedProducts", new String[]{
                "Apache HTTP Server",
                "Apache Tomcat",
                "IBM WebSphere",
                "Oracle WebLogic"
        });

        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("swagger", "/swagger-ui.html");
        endpoints.put("h2Console", "/h2-console");
        endpoints.put("webInterface", "/static/index.html");
        endpoints.put("apiDocs", "/api-docs");
        info.put("endpoints", endpoints);

        return ResponseEntity.ok(info);
    }

    @GetMapping("/database/status")
    @Operation(
            summary = "Get database status",
            description = "Check database connectivity and data integrity"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Database status information"
    )
    public ResponseEntity<Map<String, Object>> getDatabaseStatus() {
        Map<String, Object> status = new HashMap<>();

        try {
            status.put("connected", true);
            status.put("totalProducts", productRepository.count());
            status.put("totalTickets", ticketRepository.count());
            status.put("totalUpdates", ticketUpdateRepository.count());

            // Data integrity checks
            long ticketsWithProducts = ticketRepository.findAll().stream()
                    .mapToLong(ticket -> ticket.getProduct() != null ? 1 : 0)
                    .sum();

            status.put("dataIntegrity", Map.of(
                    "ticketsWithProducts", ticketsWithProducts,
                    "orphanedTickets", ticketRepository.count() - ticketsWithProducts
            ));

            return ResponseEntity.ok(status);

        } catch (Exception e) {
            status.put("connected", false);
            status.put("error", e.getMessage());
            return ResponseEntity.status(500).body(status);
        }
    }

    @PostMapping("/ai/test")
    @Operation(
            summary = "Test AI model connectivity",
            description = "Send a test query to the AI model to verify it's working correctly"
    )
    @ApiResponse(
            responseCode = "200",
            description = "AI model test result",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "AI Test Response",
                            value = """
                {
                  "status": "success",
                  "testQuery": "What is 2+2?",
                  "response": "2+2 equals 4",
                  "responseTime": 1250
                }
                """
                    )
            )
    )
    public ResponseEntity<Map<String, Object>> testAiModel(
            @RequestParam(defaultValue = "What is 2+2?") String testQuery) {
        Map<String, Object> result = new HashMap<>();

        try {
            long startTime = System.currentTimeMillis();
            String response = chatLanguageModel.generate(testQuery);
            long responseTime = System.currentTimeMillis() - startTime;

            result.put("status", "success");
            result.put("testQuery", testQuery);
            result.put("response", response.trim());
            result.put("responseTime", responseTime);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("status", "error");
            result.put("testQuery", testQuery);
            result.put("error", e.getMessage());

            return ResponseEntity.status(500).body(result);
        }
    }

    @GetMapping("/metrics")
    @Operation(
            summary = "Get application metrics",
            description = "Returns various application metrics and usage statistics"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Application metrics"
    )
    public ResponseEntity<Map<String, Object>> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        // Ticket metrics
        Map<String, Object> ticketMetrics = new HashMap<>();
        ticketMetrics.put("totalTickets", ticketRepository.count());
        ticketMetrics.put("resolvedTickets", ticketRepository.findByStatus("Resolved").size());
        ticketMetrics.put("inProgressTickets", ticketRepository.findByStatus("In Progress").size());
        ticketMetrics.put("highPriorityTickets", ticketRepository.findByPriority("High").size());
        ticketMetrics.put("criticalTickets", ticketRepository.findByPriority("Critical").size());

        // Product metrics
        Map<String, Object> productMetrics = new HashMap<>();
        productMetrics.put("totalProducts", productRepository.count());
        productMetrics.put("webServers", productRepository.findByCategory("Web Server").size());
        productMetrics.put("applicationServers", productRepository.findByCategory("Application Server").size());

        metrics.put("tickets", ticketMetrics);
        metrics.put("products", productMetrics);
        metrics.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return ResponseEntity.ok(metrics);
    }
}