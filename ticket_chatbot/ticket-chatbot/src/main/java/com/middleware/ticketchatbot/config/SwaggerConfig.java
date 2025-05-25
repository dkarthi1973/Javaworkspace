// SwaggerConfig.java
package com.middleware.ticketchatbot.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Middleware Support Chatbot API")
                        .description("""
                                ## 🤖 Middleware Support Chatbot with RAG
                                
                                This API provides intelligent support for middleware products using the RAG (Retrieval-Augmented Generation) pattern.
                                
                                ### 🏗️ **Architecture:**
                                - **Database**: H2 in-memory database with ServiceNow tickets
                                - **AI Model**: Llama3 running locally via Ollama
                                - **RAG Pattern**: Retrieves relevant tickets and generates AI responses
                                - **Products Supported**: Apache HTTP Server, Apache Tomcat, IBM WebSphere, Oracle WebLogic
                                
                                ### 🚀 **Key Features:**
                                - **Intelligent Query Processing**: Ask questions in natural language
                                - **Historical Knowledge**: Learns from past ServiceNow tickets
                                - **Local Processing**: No external API calls, complete privacy
                                - **Real-time Responses**: Fast ticket retrieval and AI generation
                                
                                ### 📚 **How to Use:**
                                1. **Chat API**: Send questions to `/api/chat/query` 
                                2. **Tickets API**: Browse and search historical tickets
                                3. **Database Console**: Access H2 console at `/h2-console`
                                4. **Web Interface**: Use the chatbot UI at `/static/index.html`
                                
                                ### 🧪 **Try These Sample Queries:**
                                - "How do I fix Apache server startup issues?"
                                - "What causes Tomcat OutOfMemoryError?"
                                - "Show me WebSphere cluster problems"
                                - "What are common WebLogic performance issues?"
                                - "List all high priority tickets"
                                
                                ### 🔧 **Prerequisites:**
                                - Java 17+
                                - Maven 3.6+
                                - Ollama with Llama3 model
                                - Windows 10/11 or Linux/macOS
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Middleware Support Team")
                                .email("support@company.com")
                                .url("https://company.com/middleware-support"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server"),
                        new Server()
                                .url("https://your-domain.com")
                                .description("Production Server (if deployed)")
                ))
                .tags(List.of(
                        new Tag()
                                .name("Chatbot")
                                .description("🤖 **AI Chatbot Operations** - Send queries and get intelligent responses based on historical tickets"),
                        new Tag()
                                .name("Tickets")
                                .description("🎫 **Ticket Management** - Browse, search, and retrieve ServiceNow tickets"),
                        new Tag()
                                .name("Products")
                                .description("📦 **Middleware Products** - Information about supported middleware products"),
                        new Tag()
                                .name("Health")
                                .description("❤️ **System Health** - Monitor application status and connectivity")
                ));
    }
}
