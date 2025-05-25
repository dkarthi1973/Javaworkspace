// ChatbotService.java
package com.middleware.ticketchatbot.service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {

    private final ChatLanguageModel chatLanguageModel;
    private final TicketRetriever ticketRetriever;

    public String processQuery(String userQuery) {
        try {
            log.info("Processing user query: {}", userQuery);

            // Step 1: Retrieve relevant ticket data from database
            List<String> relevantTickets = ticketRetriever.retrieveRelevantTicketData(userQuery);

            if (relevantTickets.isEmpty()) {
                return "I couldn't find any relevant tickets or information related to your query. " +
                        "Please try rephrasing your question or ask about specific middleware products like Apache, Tomcat, WebSphere, or WebLogic.";
            }

            // Step 2: Build context for the LLM
            String context = buildContext(relevantTickets);

            // Step 3: Create prompt for the LLM
            String prompt = buildPrompt(userQuery, context);

            log.info("Sending prompt to LLM with {} relevant tickets", relevantTickets.size());

            // Step 4: Get response from LLM
            String response = chatLanguageModel.generate(prompt);

            log.info("Received response from LLM");

            return response;

        } catch (Exception e) {
            log.error("Error processing query: {}", e.getMessage(), e);
            return "I apologize, but I encountered an error while processing your request. Please try again or contact support if the issue persists.";
        }
    }

    private String buildContext(List<String> relevantTickets) {
        StringBuilder context = new StringBuilder();
        context.append("Here are the relevant ServiceNow tickets from our middleware support database:\n\n");

        for (int i = 0; i < relevantTickets.size(); i++) {
            context.append("Ticket ").append(i + 1).append(":\n");
            context.append(relevantTickets.get(i));
        }

        return context.toString();
    }

    private String buildPrompt(String userQuery, String context) {
        return String.format("""
            You are a helpful middleware support expert assistant. You have access to a database of ServiceNow tickets 
            related to middleware products (Apache HTTP Server, Apache Tomcat, IBM WebSphere, Oracle WebLogic).
            
            Your role is to:
            1. Answer questions about middleware issues based on the provided ticket data
            2. Provide helpful troubleshooting guidance
            3. Reference specific tickets when relevant
            4. Suggest solutions based on past resolutions
            5. Be concise but thorough in your responses
            
            Context (Relevant tickets from database):
            %s
            
            User Question: %s
            
            Please provide a helpful response based on the ticket information above. If you reference specific tickets, 
            mention their ticket numbers. If no directly relevant information is found, provide general guidance 
            based on your knowledge of middleware technologies.
            
            Response:
            """, context, userQuery);
    }
}
