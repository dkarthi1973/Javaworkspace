Document QA - Spring Boot Project

This project is a simple document-based Q&A system built with Spring Boot and LangChain4j. It allows uploading PDF documents, processing them, and asking questions based on the document content.

How to Run the Project

Prerequisites

    Java 17 or higher installed

    Maven or Gradle build tool

    Ollama or compatible LLM configured (e.g., Llama 3.1)

    Clone this repository and navigate to the document-qa folder

Build and Run

From the project root (document-qa folder), run:

text
./mvnw clean spring-boot:run



The application will start on http://localhost:8080.

API Endpoints

    Upload a PDF Document (Upload a PDF file to the server for processing.)

command
curl -X POST -H "Content-Type: multipart/form-data" -F "file=@/path/to/your/document.pdf" http://localhost:8080/api/documents/upload

    List All Uploaded Documents (Retrieve a list of all uploaded and processed documents.)

command
curl -X GET -H "Content-Type: application/json" http://localhost:8080/api/documents

    Ask a Question About Documents (Send a question to the system to get answers based on uploaded documents.)

command
curl -X POST -H "Content-Type: application/json" -d '{"question":"What are the main points discussed in the document?"}' http://localhost:8080/api/documents/ask

    Check Document Processing Diagnostics(Check the status and diagnostics of uploaded documents.)

command
curl -X GET -H "Content-Type: application/json" http://localhost:8080/api/documents/diagnostics
