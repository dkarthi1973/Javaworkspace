// ChatResponse.java (Updated with Swagger annotations)
package com.middleware.ticketchatbot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Response object containing the chatbot's answer and success status")
public class ChatResponse {

    @Schema(
            description = "The chatbot's response to the user's query",
            example = "Based on ticket INC000123456, Apache server startup issues are commonly caused by configuration syntax errors..."
    )
    private String response;

    @Schema(
            description = "Indicates whether the request was processed successfully",
            example = "true"
    )
    private boolean success;
}