package com.example.documentqa.controller;

import com.example.documentqa.model.Document;
import com.example.documentqa.model.QuestionRequest;
import com.example.documentqa.repository.DocumentRepository;
import com.example.documentqa.service.DocumentService;
import com.example.documentqa.service.QaService;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentQaController {

    private final DocumentService documentService;
    private final QaService qaService;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final DocumentRepository documentRepository;
    @PostMapping("/upload")
    public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            Document savedDocument = documentService.saveDocument(file);

            // Process the document immediately
            try {
                documentService.processDocument(savedDocument);
                savedDocument.setProcessed(true);
                documentRepository.save(savedDocument);
                return ResponseEntity.ok("Document uploaded and processed successfully with ID: " + savedDocument.getId());
            } catch (Exception e) {
                log.error("Error processing document", e);
                return ResponseEntity.ok("Document uploaded with ID: " + savedDocument.getId() + ", but processing failed: " + e.getMessage());
            }
        } catch (IOException e) {
            log.error("Error uploading document", e);
            return ResponseEntity.badRequest().body("Failed to upload document: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Document>> getAllDocuments() {
        return ResponseEntity.ok(documentService.getAllDocuments());
    }

    @PostMapping("/ask")
    public ResponseEntity<String> askQuestion(@RequestBody QuestionRequest request) {
        try {
            String answer = qaService.answerQuestion(request.getQuestion());
            return ResponseEntity.ok(answer);
        } catch (Exception e) {
            log.error("Error processing question", e);
            return ResponseEntity.badRequest().body("Failed to process question: " + e.getMessage());
        }
    }
    @GetMapping("/diagnostics")
    public ResponseEntity<Map<String, Object>> getDiagnostics() {
        Map<String, Object> diagnostics = new HashMap<>();

        // Skip embedding store check if no suitable method is available

        // Check documents
        List<Document> documents = documentService.getAllDocuments();
        List<Map<String, Object>> docInfo = documents.stream()
                .map(doc -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("id", doc.getId());
                    info.put("title", doc.getTitle());
                    info.put("processed", doc.isProcessed());
                    return info;
                })
                .collect(Collectors.toList());

        diagnostics.put("documents", docInfo);
        diagnostics.put("documentCount", documents.size());
        diagnostics.put("processedDocuments", documents.stream().filter(Document::isProcessed).count());

        return ResponseEntity.ok(diagnostics);
    }
}