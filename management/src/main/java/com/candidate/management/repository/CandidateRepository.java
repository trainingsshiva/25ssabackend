package com.candidate.management.repository;

import com.candidate.management.entity.Candidate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    Optional<Candidate> findByReceiptNumber(String receiptNumber);

    @Query("SELECT COUNT(c) > 0 FROM Candidate c WHERE c.receiptNumber = :receiptNumber AND (c.hibernationEndDate IS NULL OR c.hibernationEndDate >= CURRENT_DATE)")
    boolean existsByReceiptNumberAndActive(@Param("receiptNumber") String receiptNumber);

    @Query("SELECT COUNT(c) > 0 FROM Candidate c WHERE c.receiptNumber = :receiptNumber AND (c.hibernationEndDate IS NULL OR c.hibernationEndDate >= CURRENT_DATE) AND c.id <> :id")
    boolean existsByReceiptNumberAndActiveAndIdNot(@Param("receiptNumber") String receiptNumber, @Param("id") Long id);

    @Query("SELECT c FROM Candidate c WHERE " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:search IS NULL OR LOWER(c.receiptNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.petitionerName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Candidate> searchCandidates(@Param("search") String search, @Param("status") String status, Pageable pageable);

    // Queries for Alerts
    @Query("SELECT c FROM Candidate c WHERE c.expiryDate >= :start AND c.expiryDate <= :end")
    List<Candidate> findCandidatesExpiringBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);



    // Counts for stats
    long countByStatus(String status);
}
