package com.candidate.management.service;

import com.candidate.management.entity.Candidate;
import com.candidate.management.entity.StatusHistory;
import com.candidate.management.exception.DuplicateReceiptException;
import com.candidate.management.exception.ResourceNotFoundException;
import com.candidate.management.repository.CandidateRepository;
import com.candidate.management.repository.StatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CandidateService {

    private final CandidateRepository candidateRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final AuditLogService auditLogService;

    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equalsIgnoreCase("anonymousUser")) {
            return auth.getName();
        }
        return "SYSTEM";
    }

    public Page<Candidate> getCandidates(String search, String status, Pageable pageable) {
        // Clean parameters
        String searchParam = (search == null || search.trim().isEmpty()) ? null : search.trim();
        String statusParam = (status == null || status.trim().isEmpty() || "All".equalsIgnoreCase(status)) ? null : status;
        return candidateRepository.searchCandidates(searchParam, statusParam, pageable);
    }

    public Candidate getCandidateById(Long id) {
        return candidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id: " + id));
    }

    @Transactional
    public Candidate createCandidate(Candidate candidate) {
        if (candidateRepository.existsByReceiptNumberAndActive(candidate.getReceiptNumber())) {
            throw new DuplicateReceiptException("Receipt number '" + candidate.getReceiptNumber() + "' is already registered and active.");
        }

        // Validate expiry date vs start date
        if (candidate.getStartDate() != null && candidate.getNoticeDate() != null) {
            LocalDate calculatedExpiry = candidate.getNoticeDate().plusMonths(2);
            if (!calculatedExpiry.isAfter(candidate.getStartDate())) {
                throw new IllegalArgumentException("Calculated expiry date must be strictly after the start date.");
            }
        }

        // Auto-calculate dates
        candidate.calculateDates();
        Candidate savedCandidate = candidateRepository.save(candidate);

        // Record Status History
        StatusHistory statusHistory = StatusHistory.builder()
                .candidate(savedCandidate)
                .status(savedCandidate.getStatus())
                .changedBy(getCurrentUser())
                .changeReason("Initial registration status")
                .build();
        statusHistoryRepository.save(statusHistory);

        // Audit Log
        auditLogService.log("Candidate", savedCandidate.getId(), "CREATE", 
                "Registered candidate: " + savedCandidate.getFirstName() + " " + savedCandidate.getLastName() + 
                " (Receipt: " + savedCandidate.getReceiptNumber() + ")");

        return savedCandidate;
    }

    @Transactional
    public Candidate updateCandidate(Long id, Candidate details, String changeReason) {
        Candidate candidate = getCandidateById(id);

        // Check unique active receipt number if changed
        if (!candidate.getReceiptNumber().equals(details.getReceiptNumber())) {
            if (candidateRepository.existsByReceiptNumberAndActiveAndIdNot(details.getReceiptNumber(), id)) {
                throw new DuplicateReceiptException("Receipt number '" + details.getReceiptNumber() + "' is already in use by another active candidate.");
            }
        }

        // Validate expiry date vs start date
        if (details.getStartDate() != null && details.getNoticeDate() != null) {
            LocalDate calculatedExpiry = details.getNoticeDate().plusMonths(2);
            if (!calculatedExpiry.isAfter(details.getStartDate())) {
                throw new IllegalArgumentException("Calculated expiry date must be strictly after the start date.");
            }
        }

        boolean statusChanged = !candidate.getStatus().equalsIgnoreCase(details.getStatus());
        String oldStatus = candidate.getStatus();

        // Update fields
        candidate.setReceiptNumber(details.getReceiptNumber());
        candidate.setFirstName(details.getFirstName());
        candidate.setMiddleName(details.getMiddleName());
        candidate.setLastName(details.getLastName());
        candidate.setPetitionerName(details.getPetitionerName());
        candidate.setDateOfBirth(details.getDateOfBirth());
        candidate.setReceiveDate(details.getReceiveDate());
        candidate.setNoticeDate(details.getNoticeDate());
        candidate.setStatus(details.getStatus());
        candidate.setPlacementDate(details.getPlacementDate());
        candidate.setStartDate(details.getStartDate());

        // Auto-calculate dates
        candidate.calculateDates();
        Candidate updatedCandidate = candidateRepository.save(candidate);

        // Record Status History if changed
        if (statusChanged) {
            StatusHistory statusHistory = StatusHistory.builder()
                    .candidate(updatedCandidate)
                    .status(updatedCandidate.getStatus())
                    .changedBy(getCurrentUser())
                    .changeReason(changeReason != null && !changeReason.trim().isEmpty() ? changeReason : "Status changed from " + oldStatus + " to " + updatedCandidate.getStatus())
                    .build();
            statusHistoryRepository.save(statusHistory);
        }

        // Audit Log
        auditLogService.log("Candidate", updatedCandidate.getId(), "UPDATE", 
                "Updated candidate: " + updatedCandidate.getFirstName() + " " + updatedCandidate.getLastName() + 
                " (Receipt: " + updatedCandidate.getReceiptNumber() + ")." + 
                (statusChanged ? " Status updated from " + oldStatus + " to " + updatedCandidate.getStatus() : ""));

        return updatedCandidate;
    }

    @Transactional
    public void deleteCandidate(Long id) {
        Candidate candidate = getCandidateById(id);
        candidateRepository.delete(candidate);

        // Audit Log
        auditLogService.log("Candidate", id, "DELETE", 
                "Deleted candidate: " + candidate.getFirstName() + " " + candidate.getLastName() + 
                " (Receipt: " + candidate.getReceiptNumber() + ")");
    }

    public List<StatusHistory> getStatusHistory(Long candidateId) {
        // Confirm candidate exists
        getCandidateById(candidateId);
        return statusHistoryRepository.findByCandidateIdOrderByCreatedAtDesc(candidateId);
    }
}
