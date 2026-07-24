package com.candidate.management.repository;

import com.candidate.management.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByCandidateId(Long candidateId);

    @Query("SELECT d FROM Document d WHERE d.expiryDate >= :start AND d.expiryDate <= :end")
    List<Document> findDocumentsExpiringBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
