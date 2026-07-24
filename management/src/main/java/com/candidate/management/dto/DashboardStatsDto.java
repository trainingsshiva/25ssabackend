package com.candidate.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDto {
    private long totalCandidates;
    private long marketingCount;
    private long placedCount;
    private long hibernateCount;
    private long terminatedCount;
    private long expiringSoonCount; // Total count of active alerts
}
