package com.candidate.management.service;

import com.candidate.management.entity.Candidate;
import com.candidate.management.entity.Document;
import com.candidate.management.exception.ResourceNotFoundException;
import com.candidate.management.repository.CandidateRepository;
import com.candidate.management.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final CandidateRepository candidateRepository;
    private final AuditLogService auditLogService;

    @Value("${app.upload.dir}")
    private String uploadDir;

    public List<Document> getDocumentsByCandidate(Long candidateId) {
        if (!candidateRepository.existsById(candidateId)) {
            throw new ResourceNotFoundException("Candidate not found with id: " + candidateId);
        }
        return documentRepository.findByCandidateId(candidateId);
    }

    public Document getDocumentById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));
    }

    @Transactional
    public Document uploadDocument(
            Long candidateId,
            String documentType,
            String documentNumber,
            LocalDate issueDate,
            LocalDate expiryDate,
            String dlNumber,
            LocalDate dateOfBirth,
            String state,
            String firstName,
            String middleName,
            String lastName,
            String sex,
            String hairColor,
            String weight,
            String eyeColor,
            String height,
            MultipartFile file
    ) throws IOException {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id: " + candidateId));

        // Cross-document validation for personal details
        List<Document> existingDocs = documentRepository.findByCandidateId(candidateId);
        if (existingDocs != null && !existingDocs.isEmpty()) {
            for (Document doc : existingDocs) {
                if (firstName != null && doc.getFirstName() != null && !firstName.equalsIgnoreCase(doc.getFirstName())) {
                    throw new IllegalArgumentException("First name must match across all documents for the candidate.");
                }
                if (lastName != null && doc.getLastName() != null && !lastName.equalsIgnoreCase(doc.getLastName())) {
                    throw new IllegalArgumentException("Last name must match across all documents for the candidate.");
                }
                if (middleName != null && doc.getMiddleName() != null && !middleName.equalsIgnoreCase(doc.getMiddleName())) {
                    throw new IllegalArgumentException("Middle name must match across all documents for the candidate.");
                }
                if (dateOfBirth != null && doc.getDateOfBirth() != null && !dateOfBirth.equals(doc.getDateOfBirth())) {
                    throw new IllegalArgumentException("Date of birth must match across all documents for the candidate.");
                }
                if (state != null && doc.getState() != null && !state.equalsIgnoreCase(doc.getState())) {
                    throw new IllegalArgumentException("State must match across all documents for the candidate.");
                }
                if (sex != null && doc.getSex() != null && !sex.equalsIgnoreCase(doc.getSex())) {
                    throw new IllegalArgumentException("Sex must match across all documents for the candidate.");
                }
                if (hairColor != null && doc.getHairColor() != null && !hairColor.equalsIgnoreCase(doc.getHairColor())) {
                    throw new IllegalArgumentException("Hair color must match across all documents for the candidate.");
                }
                if (weight != null && doc.getWeight() != null && !weight.equalsIgnoreCase(doc.getWeight())) {
                    throw new IllegalArgumentException("Weight must match across all documents for the candidate.");
                }
                if (eyeColor != null && doc.getEyeColor() != null && !eyeColor.equalsIgnoreCase(doc.getEyeColor())) {
                    throw new IllegalArgumentException("Eye color must match across all documents for the candidate.");
                }
            }
        }

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Clean file name and create a unique stored name
        String originalFilename = file.getOriginalFilename();
        String cleanFileName = originalFilename != null ? originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_") : "document";
        String uniqueFileName = candidateId + "_" + System.currentTimeMillis() + "_" + cleanFileName;
        Path targetLocation = uploadPath.resolve(uniqueFileName);

        // Copy file
        Files.copy(file.getInputStream(), targetLocation);

        // Create entity
        Document document = Document.builder()
                .candidate(candidate)
                .documentType(documentType)
                .documentNumber(documentNumber)
                .issueDate(issueDate)
                .expiryDate(expiryDate)
                .dlNumber(dlNumber)
                .dateOfBirth(dateOfBirth)
                .state(state)
                .firstName(firstName)
                .middleName(middleName)
                .lastName(lastName)
                .sex(sex)
                .hairColor(hairColor)
                .weight(weight)
                .eyeColor(eyeColor)
                .height(height)
                .filePath(targetLocation.toString())
                .fileName(originalFilename)
                .build();

        Document savedDoc = documentRepository.save(document);

        // Audit Log
        auditLogService.log("Document", savedDoc.getId(), "CREATE",
                "Uploaded DL Document: Type=" + documentType + ", Number=" + documentNumber + 
                ", File=" + originalFilename + " for candidate: " + candidate.getFirstName() + " " + candidate.getLastName());

        return savedDoc;
    }

    public Resource loadFileAsResource(Long documentId) {
        Document document = getDocumentById(documentId);
        try {
            Path filePath = Paths.get(document.getFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File not found on server disk: " + document.getFileName());
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("File path URI is malformed: " + document.getFileName());
        }
    }

    @Transactional
    public void deleteDocument(Long id) {
        Document document = getDocumentById(id);

        // Delete physical file
        try {
            Path filePath = Paths.get(document.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log warning but continue entity deletion
            System.err.println("Warning: Failed to delete physical file: " + document.getFilePath());
        }

        // Delete entity
        documentRepository.delete(document);

        // Audit Log
        auditLogService.log("Document", id, "DELETE",
                "Deleted DL Document: Type=" + document.getDocumentType() + ", Number=" + document.getDocumentNumber() + 
                ", File=" + document.getFileName() + " of Candidate ID: " + document.getCandidate().getId());
    }
}
