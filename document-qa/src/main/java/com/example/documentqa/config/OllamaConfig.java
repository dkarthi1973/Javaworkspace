// src/main/java/com/example/documentqa/config/OllamaConfig.java
package com.example.documentqa.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class OllamaConfig {

    @Value("${ollama.base.url}")
    private String ollamaBaseUrl;

    @Value("${ollama.model.name}")
    private String ollamaModelName;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OllamaChatModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName(ollamaModelName)
                .temperature(0.1)
                .timeout(Duration.ofMinutes(10)) // Increase timeout to 5 minutes
                .build();
    }

    @Bean
    public OllamaEmbeddingModel ollamaEmbeddingModel() {
        return OllamaEmbeddingModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName(ollamaModelName)
                .timeout(Duration.ofMinutes(10)) // Increase timeout to 5 minutes
                .build();
    }
}