package com.candidate.management.service;

import com.candidate.management.dto.AlertDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpirySchedulerService {

    private static final Logger log = LoggerFactory.getLogger(ExpirySchedulerService.class);
    private final DashboardService dashboardService;
    private final EmailService emailService;

    // Automated scheduler: runs daily at 8:00 AM
    @Scheduled(cron = "0 0 8 * * *")
    public void scanAndSendAlerts() {
        log.info("Starting scheduled candidate tracking and DL expiry check...");
        List<AlertDto> activeAlerts = dashboardService.getActiveAlerts();

        if (activeAlerts.isEmpty()) {
            log.info("Scheduled scan complete: No expiring candidates or documents found.");
            return;
        }

        log.info("Scheduled scan completed. Found {} active alerts. Sending notification email digest...", activeAlerts.size());

        StringBuilder body = new StringBuilder();
        body.append("Hello Admin,\n\n");
        body.append("The Candidate Management System has scanned the database and detected the following upcoming expiries:\n\n");
        body.append("Active Alerts:\n");
        body.append("----------------------------------------------------------------------------------------------------\n");

        for (AlertDto alert : activeAlerts) {
            body.append(String.format("[%s] [%s] - Candidate ID %d (%s): %s\n",
                    alert.getSeverity(),
                    alert.getType(),
                    alert.getCandidateId(),
                    alert.getCandidateName(),
                    alert.getMessage()));
        }

        body.append("----------------------------------------------------------------------------------------------------\n\n");
        body.append("Please log in to the portal to review and manage these records.\n\n");
        body.append("Regards,\nCandidate Management System Scheduler");

        emailService.sendEmailAlert(
                "recruiter-alerts@candidate-management.com",
                "Automated CMS Expiry Summary: " + activeAlerts.size() + " alert(s) found",
                body.toString()
        );
    }
}
