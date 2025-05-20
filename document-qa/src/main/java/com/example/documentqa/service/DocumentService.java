package com.example.documentqa.service;

import com.example.documentqa.model.Document;
import com.example.documentqa.repository.DocumentRepository;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;
    private final DocumentSplitter documentSplitter;

    public Document saveDocument(MultipartFile file) throws IOException {
        Document document = Document.builder()
                .title(file.getOriginalFilename())
                .filename(file.getOriginalFilename())
                .content(file.getBytes())
                .contentType(file.getContentType())
                .uploadedAt(LocalDateTime.now())
                .processed(false)
                .build();

        return documentRepository.save(document);
    }

    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    @Scheduled(fixedRate = 60000) // Run every minute
    public void processUnprocessedDocuments() {
        List<Document> unprocessedDocuments = documentRepository.findByProcessed(false);

        log.info("Found {} unprocessed documents", unprocessedDocuments.size());

        for (Document document : unprocessedDocuments) {
            try {
                log.info("Processing document: {}", document.getTitle());
                processDocument(document);

                document.setProcessed(true);
                documentRepository.save(document);

                log.info("Successfully processed document: {}", document.getTitle());
            } catch (Exception e) {
                log.error("Error processing document {}: {}", document.getTitle(), e.getMessage(), e);
                // Print full stack trace for better debugging
                e.printStackTrace();
            }
        }
    }

    public void processDocument(Document document) throws IOException {
        if (!"application/pdf".equals(document.getContentType())) {
            throw new IllegalArgumentException("Only PDF documents are supported");
        }

        log.info("Starting PDF parsing for document: {}", document.getTitle());

        // Convert byte[] to InputStream
        InputStream inputStream = new ByteArrayInputStream(document.getContent());

        DocumentParser documentParser = new ApachePdfBoxDocumentParser();
        dev.langchain4j.data.document.Document parsedDocument = documentParser.parse(inputStream);

        String text = parsedDocument.text();
        log.info("PDF parsed successfully, text length: {}", text.length());

        // Create much larger chunks to reduce the number of embeddings needed
        List<TextSegment> segments = new ArrayList<>();
        int chunkSize = 2000;  // Using larger chunks

        // Split into larger fixed-size chunks
        for (int i = 0; i < text.length(); i += chunkSize) {
            int end = Math.min(i + chunkSize, text.length());
            String chunk = text.substring(i, end).trim();
            if (!chunk.isEmpty()) {
                segments.add(TextSegment.from(chunk));
            }
        }

        log.info("Document split into {} segments", segments.size());

        if (segments.isEmpty()) {
            log.warn("No segments extracted from document");
            return;
        }

        // Process one segment at a time to avoid overwhelming Ollama
        for (int i = 0; i < segments.size(); i++) {
            try {
                log.info("Processing segment {} of {}", i+1, segments.size());

                // Create a list with a single segment
                List<TextSegment> singleSegment = List.of(segments.get(i));

                // Embed and store one segment at a time
                embeddingStore.addAll(embeddingModel.embedAll(singleSegment).content(), singleSegment);

                log.info("Successfully processed segment {} of {}", i+1, segments.size());
            } catch (Exception e) {
                log.error("Error processing segment {} of {}: {}", i+1, segments.size(), e.getMessage(), e);
                // Continue with the next segment rather than failing completely
            }
        }

        log.info("Document processing completed. {} segments were attempted.", segments.size());
    }
}