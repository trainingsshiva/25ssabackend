package com.candidate.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertDto {
    private String id; // Unique string identifier
    private String type; // CANDIDATE_EXPIRY, PROBATION_EXPIRY, DL_EXPIRY
    private String message;
    private String severity; // DANGER, WARNING
    private Long candidateId;
    private String candidateName;
    private long daysRemaining;
    private LocalDate expiryDate;
}
