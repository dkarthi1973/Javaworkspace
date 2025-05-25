// ChatbotController.java
 package com.middleware.ticketchatbot.controller;

import com.middleware.ticketchatbot.dto.ChatRequest;
import com.middleware.ticketchatbot.dto.ChatResponse;
import com.middleware.ticketchatbot.service.ChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Chatbot", description = "AI-powered chatbot for middleware support")
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/query")
    @Operation(
            summary = "Send a query to the chatbot",
            description = """
            Send a natural language question to the AI chatbot. The chatbot will:
            1. Search the ticket database for relevant historical issues
            2. Use the RAG pattern to combine your question with found tickets
            3. Generate an intelligent response using the local Llama3 model
            
            **Example queries:**
            - How do I fix Apache server startup issues?
            - What causes Tomcat OutOfMemoryError?
            - Show me WebSphere cluster problems
            - What are common WebLogic performance issues?
            """)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully processed the query",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ChatResponse.class),
                            examples = @ExampleObject(
                                    name = "Successful Response",
                                    value = """
                    {
                      "response": "Based on ticket INC000123456, Apache server startup issues are commonly caused by configuration syntax errors. The most frequent issue is typos in the httpd.conf file, particularly in VirtualHost directives. To resolve: 1) Check the error log for syntax errors, 2) Validate configuration with 'httpd -t', 3) Fix any typos or missing directives.",
                      "success": true
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request - empty or null message",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Bad Request",
                                    value = """
                    {
                      "response": "Please provide a valid question.",
                      "success": false
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error - AI model or database issue"
            )
    })
    public ResponseEntity<ChatResponse> processQuery(
            @Parameter(
                    description = "The chat request containing the user's question",
                    required = true,
                    schema = @Schema(implementation = ChatRequest.class)
            )
            @RequestBody ChatRequest request) {

        log.info("Received chat request: {}", request.getMessage());

        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ChatResponse("Please provide a valid question.", false));
        }

        try {
            String response = chatbotService.processQuery(request.getMessage().trim());
            return ResponseEntity.ok(new ChatResponse(response, true));
        } catch (Exception e) {
            log.error("Error processing chat request", e);
            return ResponseEntity.internalServerError()
                    .body(new ChatResponse("An error occurred while processing your request.", false));
        }
    }

    @GetMapping("/health")
    @Operation(
            summary = "Check chatbot health",
            description = "Returns the health status of the chatbot service including AI model connectivity"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Chatbot service is healthy",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(value = "Chatbot service is running")
                    )
            )
    })
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Chatbot service is running");
    }

    @GetMapping("/examples")
    @Operation(
            summary = "Get example queries",
            description = "Returns a list of example queries that work well with the chatbot"
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of example queries",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Example Queries",
                            value = """
                [
                  "How do I fix Apache server startup issues?",
                  "What causes Tomcat OutOfMemoryError and how to resolve it?",
                  "How to troubleshoot WebSphere cluster synchronization problems?",
                  "What are common causes of WebLogic stuck threads?",
                  "Show me all high priority tickets",
                  "What SSL certificate issues have been reported?",
                  "How to resolve JDBC connection pool problems?",
                  "What are the most common middleware performance issues?"
                ]
                """
                    )
            )
    )
    public ResponseEntity<String[]> getExampleQueries() {
        String[] examples = {
                "How do I fix Apache server startup issues?",
                "What causes Tomcat OutOfMemoryError and how to resolve it?",
                "How to troubleshoot WebSphere cluster synchronization problems?",
                "What are common causes of WebLogic stuck threads?",
                "Show me all high priority tickets",
                "What SSL certificate issues have been reported?",
                "How to resolve JDBC connection pool problems?",
                "What are the most common middleware performance issues?"
        };
        return ResponseEntity.ok(examples);
    }
}