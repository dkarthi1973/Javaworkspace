// ProductController.java
package com.middleware.ticketchatbot.controller;

import com.middleware.ticketchatbot.entity.Product;
import com.middleware.ticketchatbot.repository.ProductRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import java.util.Map;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Products", description = "Middleware product information and management")
public class ProductController {

    private final ProductRepository productRepository;

    @GetMapping
    @Operation(
            summary = "Get all middleware products",
            description = "Retrieve information about all supported middleware products including Apache, Tomcat, WebSphere, and WebLogic"
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of all products",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Products List",
                            value = """
                [
                  {
                    "id": 1,
                    "name": "Apache HTTP Server",
                    "version": "2.4.57",
                    "category": "Web Server",
                    "description": "Apache HTTP Server is a free and open-source web server that delivers web content through the internet."
                  },
                  {
                    "id": 2,
                    "name": "Apache Tomcat",
                    "version": "10.1.17",
                    "category": "Application Server",
                    "description": "Apache Tomcat is an open-source implementation of the Jakarta Servlet, Jakarta Expression Language, and WebSocket technologies."
                  }
                ]
                """
                    )
            )
    )
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get product by ID",
            description = "Retrieve detailed information about a specific middleware product"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Product found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Product.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found"
            )
    })
    public ResponseEntity<Product> getProductById(
            @Parameter(description = "Unique identifier of the product", example = "1")
            @PathVariable Long id) {
        Optional<Product> product = productRepository.findById(id);
        return product.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}")
    @Operation(
            summary = "Get product by name",
            description = "Find a product by its exact name"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Product found or empty if not found"
    )
    public ResponseEntity<Product> getProductByName(
            @Parameter(
                    description = "Exact name of the product",
                    example = "Apache Tomcat",
                    schema = @Schema(allowableValues = {
                            "Apache HTTP Server",
                            "Apache Tomcat",
                            "IBM WebSphere",
                            "Oracle WebLogic"
                    })
            )
            @PathVariable String name) {
        Optional<Product> product = productRepository.findByName(name);
        return product.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{category}")
    @Operation(
            summary = "Get products by category",
            description = "Filter products by their category type"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Products in the specified category"
    )
    public List<Product> getProductsByCategory(
            @Parameter(
                    description = "Category of the products",
                    example = "Application Server",
                    schema = @Schema(allowableValues = {
                            "Web Server",
                            "Application Server"
                    })
            )
            @PathVariable String category) {
        return productRepository.findByCategory(category);
    }

    @GetMapping("/{id}/tickets/count")
    @Operation(
            summary = "Get ticket count for a product",
            description = "Get the total number of tickets associated with a specific product"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Number of tickets for the product",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Ticket Count",
                            value = """
                {
                  "productId": 1,
                  "productName": "Apache HTTP Server",
                  "ticketCount": 2
                }
                """
                    )
            )
    )
    public ResponseEntity<Object> getProductTicketCount(
            @Parameter(description = "Product ID", example = "1")
            @PathVariable Long id) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            Product p = product.get();
            return ResponseEntity.ok(new Object() {
                public final Long productId = p.getId();
                public final String productName = p.getName();
                public final int ticketCount = p.getTickets().size();
            });
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/summary")
    @Operation(
            summary = "Get products summary",
            description = "Get a summary of all products with ticket counts and categories"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Products summary",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Products Summary",
                            value = """
                [
                  {
                    "id": 1,
                    "name": "Apache HTTP Server",
                    "version": "2.4.57",
                    "category": "Web Server",
                    "ticketCount": 2,
                    "resolvedTickets": 1,
                    "openTickets": 1
                  },
                  {
                    "id": 2,
                    "name": "Apache Tomcat",
                    "version": "10.1.17",
                    "category": "Application Server",
                    "ticketCount": 2,
                    "resolvedTickets": 1,
                    "openTickets": 1
                  }
                ]
                """
                    )
            )
    )
    public List<Object> getProductsSummary() {
        return productRepository.findAll().stream()
                .map(product -> {
                    Map<String, Object> summary = new HashMap<>();
                    summary.put("id", product.getId());
                    summary.put("name", product.getName());
                    summary.put("version", product.getVersion());
                    summary.put("category", product.getCategory());
                    summary.put("ticketCount", product.getTickets().size());
                    summary.put("resolvedTickets", product.getTickets().stream()
                            .filter(ticket -> "Resolved".equals(ticket.getStatus()))
                            .count());
                    summary.put("openTickets", product.getTickets().stream()
                            .filter(ticket -> !"Resolved".equals(ticket.getStatus()))
                            .count());
                    return summary;
                })
                .collect(Collectors.toList());
    }


}