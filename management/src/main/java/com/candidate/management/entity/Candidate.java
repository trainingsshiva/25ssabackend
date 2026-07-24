package com.candidate.management.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "candidates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "receipt_number", nullable = false, length = 50)
    private String receiptNumber;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "middle_name", length = 50)
    private String middleName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "petitioner_name", nullable = false, length = 100)
    private String petitionerName;

    @Column(name = "date_of_birth")
    @JsonFormat(pattern = "MM-dd-yyyy")
    private LocalDate dateOfBirth;

    @Column(name = "receive_date")
    @JsonFormat(pattern = "MM-dd-yyyy")
    private LocalDate receiveDate;

    @Column(name = "notice_date")
    @JsonFormat(pattern = "MM-dd-yyyy")
    private LocalDate noticeDate;

    @Column(name = "expiry_date")
    @JsonFormat(pattern = "MM-dd-yyyy")
    private LocalDate expiryDate;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(name = "placement_date")
    @JsonFormat(pattern = "MM-dd-yyyy")
    private LocalDate placementDate;

    @Column(name = "start_date")
    @JsonFormat(pattern = "MM-dd-yyyy")
    private LocalDate startDate;

    @Column(name = "hibernation_end_date")
    @JsonFormat(pattern = "MM-dd-yyyy")
    private LocalDate hibernationEndDate;    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        calculateDates();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateDates();
    }

    public void calculateDates() {
        if (noticeDate != null) {
            this.expiryDate = noticeDate.plusMonths(2);
        }
        
        if ("Placed".equalsIgnoreCase(status) || "Terminated".equalsIgnoreCase(status)) {
            if (this.hibernationEndDate == null) {
                this.hibernationEndDate = LocalDate.now().plusMonths(2);
            }
        } else {
            this.hibernationEndDate = null;
        }
    }
}
