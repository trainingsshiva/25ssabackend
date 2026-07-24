package com.candidate.management.repository;

import com.candidate.management.entity.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {
    List<StatusHistory> findByCandidateIdOrderByCreatedAtDesc(Long candidateId);
}
