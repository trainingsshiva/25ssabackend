package com.candidate.management.service;

import com.candidate.management.dto.AlertDto;
import com.candidate.management.dto.DashboardStatsDto;
import com.candidate.management.entity.Candidate;
import com.candidate.management.entity.Document;
import com.candidate.management.repository.CandidateRepository;
import com.candidate.management.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CandidateRepository candidateRepository;
    private final DocumentRepository documentRepository;

    public List<AlertDto> getActiveAlerts() {
        List<AlertDto> alerts = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // 1. Candidate Expiry Alerts (Notice Expiry Date <= today + 7 days, status is not Terminated)
        LocalDate candidateExpiryTarget = today.plusDays(7);
        // We'll search for candidates whose expiry date is up to today+7, but we also include those already expired (e.g. today-30) so they aren't forgotten.
        List<Candidate> expiringCandidates = candidateRepository.findCandidatesExpiringBetween(today.minusDays(30), candidateExpiryTarget);
        for (Candidate c : expiringCandidates) {
            if ("Terminated".equalsIgnoreCase(c.getStatus())) {
                continue;
            }
            long daysRemaining = ChronoUnit.DAYS.between(today, c.getExpiryDate());
            String severity = daysRemaining <= 2 ? "DANGER" : "WARNING";
            String msg = String.format("Receipt visa/status for candidate %s %s is expiring on %s (%d days remaining).",
                    c.getFirstName(), c.getLastName(), c.getExpiryDate(), daysRemaining);
            if (daysRemaining < 0) {
                msg = String.format("Receipt visa/status for candidate %s %s expired on %s (%d days ago!).",
                        c.getFirstName(), c.getLastName(), c.getExpiryDate(), Math.abs(daysRemaining));
                severity = "DANGER";
            }

            alerts.add(AlertDto.builder()
                    .id("CAND_" + c.getId())
                    .type("CANDIDATE_EXPIRY")
                    .message(msg)
                    .severity(severity)
                    .candidateId(c.getId())
                    .candidateName(c.getFirstName() + " " + c.getLastName())
                    .daysRemaining(daysRemaining)
                    .expiryDate(c.getExpiryDate())
                    .build());
        }



        // 3. DL Expiry Alerts (Document Expiry Date <= today + 30 days)
        LocalDate dlExpiryTarget = today.plusDays(30);
        List<Document> expiringDocs = documentRepository.findDocumentsExpiringBetween(today.minusDays(30), dlExpiryTarget);
        for (Document d : expiringDocs) {
            Candidate c = d.getCandidate();
            if ("Terminated".equalsIgnoreCase(c.getStatus())) {
                continue;
            }
            long daysRemaining = ChronoUnit.DAYS.between(today, d.getExpiryDate());
            String severity = daysRemaining <= 7 ? "DANGER" : "WARNING";
            String msg = String.format("Driving License (%s) for candidate %s %s is expiring on %s (%d days remaining).",
                    d.getDocumentNumber(), c.getFirstName(), c.getLastName(), d.getExpiryDate(), daysRemaining);
            if (daysRemaining < 0) {
                msg = String.format("Driving License (%s) for candidate %s %s expired on %s (%d days ago!).",
                        d.getDocumentNumber(), c.getFirstName(), c.getLastName(), d.getExpiryDate(), Math.abs(daysRemaining));
                severity = "DANGER";
            }

            alerts.add(AlertDto.builder()
                    .id("DOC_" + d.getId())
                    .type("DL_EXPIRY")
                    .message(msg)
                    .severity(severity)
                    .candidateId(c.getId())
                    .candidateName(c.getFirstName() + " " + c.getLastName())
                    .daysRemaining(daysRemaining)
                    .expiryDate(d.getExpiryDate())
                    .build());
        }

        return alerts;
    }

    public DashboardStatsDto getStats() {
        long total = candidateRepository.count();
        long marketing = candidateRepository.countByStatus("Marketing");
        long placed = candidateRepository.countByStatus("Placed");
        long hibernate = candidateRepository.countByStatus("Hibernate"); // Optional, but may be 0 now since it's an implicit state via hibernation_end_date.
        long terminated = candidateRepository.countByStatus("Terminated");

        // Count of active alerts
        long alertCount = getActiveAlerts().size();

        return DashboardStatsDto.builder()
                .totalCandidates(total)
                .marketingCount(marketing)
                .placedCount(placed)
                .hibernateCount(hibernate)
                .terminatedCount(terminated)
                .expiringSoonCount(alertCount)
                .build();
    }
}
