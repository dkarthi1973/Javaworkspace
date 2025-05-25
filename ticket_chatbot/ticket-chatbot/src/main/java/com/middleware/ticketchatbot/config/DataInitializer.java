package com.middleware.ticketchatbot.config;

import com.middleware.ticketchatbot.entity.Product;
import com.middleware.ticketchatbot.entity.Ticket;
import com.middleware.ticketchatbot.entity.TicketUpdate;
import com.middleware.ticketchatbot.repository.ProductRepository;
import com.middleware.ticketchatbot.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final TicketRepository ticketRepository;
    private final Random random = new Random();

    @Override
    public void run(String... args) {

        if (productRepository.count() > 0) {
            return; // Skip initialization if data already exists
        }

        // Create Products
        Product apache = createProduct("Apache HTTP Server", "2.4.57", "Web Server",
                "Apache HTTP Server is a free and open-source web server that delivers web content through the internet.");

        Product tomcat = createProduct("Apache Tomcat", "10.1.17", "Application Server",
                "Apache Tomcat is an open-source implementation of the Jakarta Servlet, Jakarta Expression Language, and WebSocket technologies.");

        Product webSphere = createProduct("IBM WebSphere", "9.0.5", "Application Server",
                "IBM WebSphere Application Server is a flexible, security-rich Java server runtime environment for enterprise applications.");

        Product webLogic = createProduct("Oracle WebLogic", "14.1.1", "Application Server",
                "Oracle WebLogic Server is a unified and extensible platform for developing, deploying and running enterprise applications.");

        List<Product> products = List.of(apache, tomcat, webSphere, webLogic);
        productRepository.saveAll(products);
        System.out.println("List of Product added");

        // Create Tickets for each product
        for (Product product : products) {
            System.out.println("ticket added");
            createTicketsForProduct(product);
        }
    }

    private Product createProduct(String name, String version, String category, String description) {
        return Product.builder()
                .name(name)
                .version(version)
                .category(category)
                .description(description)
                .build();
    }

    private void createTicketsForProduct(Product product) {
        List<Ticket> tickets = switch (product.getName()) {
            case "Apache HTTP Server" -> createApacheTickets(product);
            case "Apache Tomcat" -> createTomcatTickets(product);
            case "IBM WebSphere" -> createWebSphereTickets(product);
            case "Oracle WebLogic" -> createWebLogicTickets(product);
            default -> List.of();
        };

        ticketRepository.saveAll(tickets);
    }

    private List<Ticket> createApacheTickets(Product product) {
        Ticket ticket1 = createTicket(
                "INC000123456",
                "Apache server failing to start after configuration change",
                "After updating the httpd.conf file with virtual host settings, the Apache server fails to start. Error log shows: AH00526: Syntax error on line 217 of /etc/httpd/conf/httpd.conf: Invalid command 'VirtualHos', perhaps misspelled or defined by a module not included in the server configuration.",
                "High",
                "Resolved",
                "John Smith",
                "Alice Johnson",
                LocalDateTime.now().minusDays(15),
                LocalDateTime.now().minusDays(14),
                LocalDateTime.now().minusDays(14),
                product,
                "The issue was resolved by correcting the typo in the httpd.conf file. Changed 'VirtualHos' to 'VirtualHost' on line 217.",
                "Production",
                "Configuration"
        );

        addTicketUpdate(ticket1, "Initial investigation started. Looking at configuration files.", "John Smith", "Comment");
        addTicketUpdate(ticket1, "Found typo in VirtualHost directive on line 217.", "John Smith", "Comment");
        addTicketUpdate(ticket1, "Changed status from In Progress to Resolved", "System", "Status Change");
        addTicketUpdate(ticket1, "Fix verified - Apache server starts correctly after typo correction.", "Alice Johnson", "Comment");

        Ticket ticket2 = createTicket(
                "INC000123789",
                "Apache server high CPU usage during peak hours",
                "Our Apache server is experiencing high CPU usage (90%+) during business hours (9am-5pm). The server becomes unresponsive and some requests time out. Server specs: 8 CPU cores, 16GB RAM, handling approximately 1000 req/second at peak.",
                "Medium",
                "In Progress",
                "Sarah Williams",
                "Bob Roberts",
                LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(2),
                null,
                product,
                null,
                "Production",
                "Performance"
        );

        addTicketUpdate(ticket2, "Initial analysis shows Worker MPM settings may need tuning.", "Sarah Williams", "Comment");
        addTicketUpdate(ticket2, "Changed MaxRequestWorkers from 250 to 150 and ServerLimit from 250 to 150.", "Sarah Williams", "Comment");
        addTicketUpdate(ticket2, "CPU usage has dropped to 70% but still monitoring.", "Sarah Williams", "Comment");
        addTicketUpdate(ticket2, "Looking into enabling HTTP/2 to reduce connection overhead.", "Sarah Williams", "Comment");

        return List.of(ticket1, ticket2);
    }

    private List<Ticket> createTomcatTickets(Product product) {
        Ticket ticket1 = createTicket(
                "INC000125678",
                "Tomcat OutOfMemoryError: Java heap space",
                "Tomcat server crashes with OutOfMemoryError: Java heap space after running for about 12 hours. Application is a Spring Boot web application with heavy database operations. Current JVM settings: -Xms1G -Xmx2G",
                "Critical",
                "Resolved",
                "Mike Wilson",
                "Karen Davis",
                LocalDateTime.now().minusDays(30),
                LocalDateTime.now().minusDays(28),
                LocalDateTime.now().minusDays(25),
                product,
                "Increased JVM heap space to -Xms2G -Xmx4G and enabled GC logging. Also fixed a memory leak in the application code where database connections weren't being properly closed in exception cases.",
                "Production",
                "Memory Management"
        );

        addTicketUpdate(ticket1, "Investigating memory usage patterns using jmap and jstat.", "Mike Wilson", "Comment");
        addTicketUpdate(ticket1, "Found memory leak in ConnectionManager class.", "Mike Wilson", "Comment");
        addTicketUpdate(ticket1, "Fixed connection leak in try-with-resources block.", "Mike Wilson", "Comment");
        addTicketUpdate(ticket1, "Increased heap size and enabled GC logging for monitoring.", "Mike Wilson", "Comment");
        addTicketUpdate(ticket1, "Solution verified. Server has been stable for 72 hours.", "Karen Davis", "Comment");

        Ticket ticket2 = createTicket(
                "INC000126789",
                "Tomcat SSL/TLS certificate expiration warning",
                "We're receiving warnings that the SSL certificate for our Tomcat server will expire in 15 days. We need to renew the certificate and update it in the Tomcat configuration without disrupting service.",
                "Medium",
                "In Progress",
                "Jennifer Lee",
                "David Brown",
                LocalDateTime.now().minusDays(7),
                LocalDateTime.now().minusDays(6),
                null,
                product,
                null,
                "Production",
                "Security"
        );

        addTicketUpdate(ticket2, "Contacted certificate authority to renew certificate.", "Jennifer Lee", "Comment");
        addTicketUpdate(ticket2, "New certificate received. Planning deployment for tonight's maintenance window.", "Jennifer Lee", "Comment");
        addTicketUpdate(ticket2, "Created backup of current keystore for rollback if needed.", "Jennifer Lee", "Comment");

        return List.of(ticket1, ticket2);
    }

    private List<Ticket> createWebSphereTickets(Product product) {
        Ticket ticket1 = createTicket(
                "INC000127890",
                "WebSphere cluster synchronization issue",
                "After deploying a new application version to our WebSphere cluster, we're seeing inconsistent behavior between nodes. Some nodes are serving the new version while others are still running the old version despite the deployment showing as successful in the admin console.",
                "High",
                "Resolved",
                "Chris Johnson",
                "Maria Garcia",
                LocalDateTime.now().minusDays(45),
                LocalDateTime.now().minusDays(44),
                LocalDateTime.now().minusDays(43),
                product,
                "The issue was caused by insufficient synchronization timeout settings. Increased the file synchronization timeout from 60s to 180s in the admin console, then performed a full resynchronization of the cluster. Also implemented a validation step in our deployment process to verify application versions across all nodes after deployment.",
                "Production",
                "Deployment"
        );

        addTicketUpdate(ticket1, "Investigating cluster configuration and deployment logs.", "Chris Johnson", "Comment");
        addTicketUpdate(ticket1, "Found timeout errors in SystemOut.log during file synchronization.", "Chris Johnson", "Comment");
        addTicketUpdate(ticket1, "Increased synchronization timeout from 60s to 180s.", "Chris Johnson", "Comment");
        addTicketUpdate(ticket1, "Performed full cluster resynchronization. All nodes now running same version.", "Chris Johnson", "Comment");
        addTicketUpdate(ticket1, "Added post-deployment validation script to check version consistency.", "Chris Johnson", "Comment");

        Ticket ticket2 = createTicket(
                "INC000128901",
                "WebSphere JDBC connection pool exhaustion",
                "Our WebSphere application is experiencing intermittent failures during peak hours with errors indicating 'Connection pool MYAPP_DB_POOL has become exhausted'. Current pool settings: minimum 10, maximum 50 connections.",
                "High",
                "In Progress",
                "Tom Anderson",
                "Patricia White",
                LocalDateTime.now().minusDays(3),
                LocalDateTime.now().minusDays(2),
                null,
                product,
                null,
                "Production",
                "Database"
        );

        addTicketUpdate(ticket2, "Analyzing connection usage patterns in PMI metrics.", "Tom Anderson", "Comment");
        addTicketUpdate(ticket2, "Found queries taking excessive time, causing connections to be held too long.", "Tom Anderson", "Comment");
        addTicketUpdate(ticket2, "Temporarily increased max connections to 75 while we optimize queries.", "Tom Anderson", "Comment");
        addTicketUpdate(ticket2, "Working with development team to optimize database access patterns.", "Tom Anderson", "Comment");

        return List.of(ticket1, ticket2);
    }

    private List<Ticket> createWebLogicTickets(Product product) {
        Ticket ticket1 = createTicket(
                "INC000129012",
                "WebLogic server stuck threads",
                "Our WebLogic server is reporting an increasing number of stuck threads. The server.log shows multiple 'BEA-000337' alerts. Application response times are degrading and some requests are timing out.",
                "Critical",
                "Resolved",
                "Lisa Campbell",
                "Ryan Thompson",
                LocalDateTime.now().minusDays(60),
                LocalDateTime.now().minusDays(59),
                LocalDateTime.now().minusDays(58),
                product,
                "Root cause was an external web service that was timing out but not properly handling connection closures. Implemented circuit breaker pattern in the application code and configured proper timeout settings in WebLogic's work manager configurations. Also added monitoring alerts for stuck threads.",
                "Production",
                "Thread Management"
        );

        addTicketUpdate(ticket1, "Analyzing thread dumps to identify stuck thread root cause.", "Lisa Campbell", "Comment");
        addTicketUpdate(ticket1, "Found threads waiting on external web service that's not responding.", "Lisa Campbell", "Comment");
        addTicketUpdate(ticket1, "Implemented circuit breaker pattern in application for the problematic service.", "Lisa Campbell", "Comment");
        addTicketUpdate(ticket1, "Configured proper timeout settings in Work Manager.", "Lisa Campbell", "Comment");
        addTicketUpdate(ticket1, "Added monitoring for stuck threads with alert threshold.", "Lisa Campbell", "Comment");
        addTicketUpdate(ticket1, "Solution verified. No stuck threads reported for 72 hours.", "Ryan Thompson", "Comment");

        Ticket ticket2 = createTicket(
                "INC000130123",
                "WebLogic JMS queue message backlog",
                "We're seeing a growing backlog of messages in our WebLogic JMS queue 'OrderProcessingQueue'. Currently over 50,000 messages in queue with only slow processing. Application logs show no errors, but messages are not being consumed fast enough.",
                "Medium",
                "In Progress",
                "Daniel Martin",
                "Susan Taylor",
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1),
                null,
                product,
                null,
                "Production",
                "JMS"
        );

        addTicketUpdate(ticket2, "Investigating consumer threads and processing rates.", "Daniel Martin", "Comment");
        addTicketUpdate(ticket2, "Found single consumer thread configured. Message processing is CPU-intensive.", "Daniel Martin", "Comment");
        addTicketUpdate(ticket2, "Increased consumer threads from 1 to 5 as temporary measure.", "Daniel Martin", "Comment");
        addTicketUpdate(ticket2, "Message backlog reducing. Working with dev team on more efficient processing logic.", "Daniel Martin", "Comment");

        return List.of(ticket1, ticket2);
    }

    private Ticket createTicket(
            String ticketNumber, String title, String description, String priority,
            String status, String assignee, String reporter, LocalDateTime createdAt,
            LocalDateTime updatedAt, LocalDateTime resolvedAt, Product product,
            String resolution, String environment, String component) {

        return Ticket.builder()
                .ticketNumber(ticketNumber)
                .title(title)
                .description(description)
                .priority(priority)
                .status(status)
                .assignee(assignee)
                .reporter(reporter)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .resolvedAt(resolvedAt)
                .product(product)
                .resolution(resolution)
                .environment(environment)
                .component(component)
                .build();
    }

    private void addTicketUpdate(Ticket ticket, String comment, String author, String updateType) {
        TicketUpdate update = TicketUpdate.builder()
                .comment(comment)
                .author(author)
                .timestamp(ticket.getCreatedAt().plusHours(random.nextInt(24)))
                .ticket(ticket)
                .updateType(updateType)
                .build();

        ticket.getUpdates().add(update);
    }
}