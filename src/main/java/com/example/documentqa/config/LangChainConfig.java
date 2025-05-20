// src/main/java/com/example/documentqa/config/LangChainConfig.java
package com.example.documentqa.config;

import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;  // Change this import
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.documentqa.service.QaService;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class LangChainConfig {

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    @Bean
    public EmbeddingStoreContentRetriever contentRetriever(EmbeddingStore<TextSegment> embeddingStore,
                                                           EmbeddingModel embeddingModel) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(5)
                .minScore(0.6)
                .build();
    }

    @Bean
    public QaService qaService(ChatLanguageModel chatLanguageModel,
                               EmbeddingStoreContentRetriever contentRetriever) {
        return AiServices.builder(QaService.class)
                .chatLanguageModel(chatLanguageModel)
                .contentRetriever(contentRetriever)
                .chatMemory(MessageWindowChatMemory.builder()  // Use this instead
                        .maxMessages(10)
                        .build())
                .build();
    }

    @Bean
    public DocumentSplitter documentSplitter() {
        // Simple implementation that splits by paragraphs
        return new DocumentSplitter() {
            @Override
            public List<TextSegment> split(dev.langchain4j.data.document.Document document) {
                String text = document.text();
                List<TextSegment> segments = new ArrayList<>();

                // Split by double newlines (paragraphs)
                String[] paragraphs = text.split("\n\n");

                for (int i = 0; i < paragraphs.length; i++) {
                    String paragraph = paragraphs[i].trim();
                    if (!paragraph.isEmpty()) {
                        segments.add(TextSegment.from(paragraph));
                    }
                }

                return segments;
            }
        };
    }
}