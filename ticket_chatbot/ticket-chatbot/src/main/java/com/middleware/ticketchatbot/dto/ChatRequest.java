// ChatRequest.java (Updated with Swagger annotations)
package com.middleware.ticketchatbot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request object for sending a message to the chatbot")
public class ChatRequest {

    @Schema(
            description = "The question or message to send to the chatbot",
            example = "How do I fix Apache server startup issues?",
            required = true,
            minLength = 1,
            maxLength = 1000
    )
    private String message;
}