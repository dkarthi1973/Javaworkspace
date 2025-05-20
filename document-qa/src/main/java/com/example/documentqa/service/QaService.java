package com.example.documentqa.service;

import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.SystemMessage;

public interface QaService {

    @SystemMessage({
            "You are an enterprise document assistant that answers questions based on the provided context.",
            "Only use the information from the retrieved documents to answer questions.",
            "If you don't know the answer based on the provided context, say so clearly.",
            "Keep answers concise, professional, and factual.",
            "Format your answers in a readable way using markdown when appropriate.",
            "If the context is insufficient, just say 'I don't have enough information to answer this question accurately.'"
    })
    String answerQuestion(@UserMessage String question);
}