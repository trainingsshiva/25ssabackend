package com.candidate.management.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "candidate")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    @JsonIgnore
    private Candidate candidate;

    @Column(name = "document_type", nullable = false, length = 50)
    private String documentType;

    @Column(name = "document_number", nullable = false, length = 50)
    private String documentNumber;

    @Column(name = "issue_date")
    @JsonFormat(pattern = "MM-dd-yyyy")
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    @JsonFormat(pattern = "MM-dd-yyyy")
    private LocalDate expiryDate;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "dl_number", length = 50)
    private String dlNumber;

    @Column(name = "date_of_birth")
    @JsonFormat(pattern = "MM-dd-yyyy")
    private LocalDate dateOfBirth;

    @Column(length = 50)
    private String state;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "middle_name", length = 50)
    private String middleName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(length = 10)
    private String sex;

    @Column(name = "hair_color", length = 20)
    private String hairColor;

    @Column(length = 10)
    private String weight;

    @Column(name = "eye_color", length = 20)
    private String eyeColor;

    @Column(length = 10)
    private String height;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
