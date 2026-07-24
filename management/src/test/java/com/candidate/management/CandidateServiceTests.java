package com.candidate.management;

import com.candidate.management.entity.Candidate;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class CandidateServiceTests {

    @Disabled
	@Test
    public void testCalculateExpiryDate() {
        // Expiry Date should automatically be calculated as 2 months from Notice Date.
        Candidate candidate = Candidate.builder()
                .firstName("Alice")
                .lastName("Smith")
                .receiptNumber("EAC1234567890")
                .status("Marketing")
                .noticeDate(LocalDate.of(2026, 6, 24))
                .build();

        candidate.calculateDates();

        // 2026-06-24 + 2 months = 2026-08-24
        assertEquals(LocalDate.of(2026, 8, 24), candidate.getExpiryDate());
    }

    @Test
    public void testCalculateProbationEndDate() {
        // When status is Probation Period, automatically set probation end date as 2 months from placement date.
        Candidate candidate = Candidate.builder()
                .firstName("Bob")
                .lastName("Jones")
                .receiptNumber("EAC9876543210")
                .status("Probation Period")
                .placementDate(LocalDate.of(2026, 5, 1))
                .build();

        candidate.calculateDates();

        // 2026-05-01 + 2 months = 2026-07-01
        assertEquals(LocalDate.of(2026, 7, 1), candidate.getHibernationEndDate());
    }

    @Test
    public void testCalculateProbationEndDate_NonProbation() {
        // If status is not Probation Period, probation end date must be null even if placement date is set.
        Candidate candidate = Candidate.builder()
                .firstName("Charlie")
                .lastName("Brown")
                .receiptNumber("EAC5555555555")
                .status("Placed")
                .placementDate(LocalDate.of(2026, 5, 1))
                .build();

        candidate.calculateDates();

        assertNull(candidate.getHibernationEndDate());
    }
}
