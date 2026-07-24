package com.candidate.management.controller;

import com.candidate.management.entity.Document;
import com.candidate.management.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<List<Document>> getDocumentsByCandidate(@PathVariable Long candidateId) {
        return ResponseEntity.ok(documentService.getDocumentsByCandidate(candidateId));
    }

    @PostMapping("/upload")
    public ResponseEntity<Document> uploadDocument(
            @RequestParam("candidateId") Long candidateId,
            @RequestParam("documentType") String documentType,
            @RequestParam("documentNumber") String documentNumber,
            @RequestParam("issueDate") @DateTimeFormat(pattern = "MM-dd-yyyy") LocalDate issueDate,
            @RequestParam("expiryDate") @DateTimeFormat(pattern = "MM-dd-yyyy") LocalDate expiryDate,
            @RequestParam(value = "dlNumber", required = false) String dlNumber,
            @RequestParam(value = "dateOfBirth", required = false) @DateTimeFormat(pattern = "MM-dd-yyyy") LocalDate dateOfBirth,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "middleName", required = false) String middleName,
            @RequestParam(value = "lastName", required = false) String lastName,
            @RequestParam(value = "sex", required = false) String sex,
            @RequestParam(value = "hairColor", required = false) String hairColor,
            @RequestParam(value = "weight", required = false) String weight,
            @RequestParam(value = "eyeColor", required = false) String eyeColor,
            @RequestParam(value = "height", required = false) String height,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        Document savedDoc = documentService.uploadDocument(
                candidateId, documentType, documentNumber, issueDate, expiryDate, 
                dlNumber, dateOfBirth, state, firstName, middleName, lastName, sex, hairColor, weight, eyeColor, height, file);
        return new ResponseEntity<>(savedDoc, HttpStatus.CREATED);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        Document document = documentService.getDocumentById(id);
        Resource resource = documentService.loadFileAsResource(id);

        String contentType = null;
        try {
            contentType = Files.probeContentType(Paths.get(document.getFilePath()));
        } catch (IOException e) {
            // Fallback content type
        }
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFileName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}
