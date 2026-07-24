package com.candidate.management.controller;

import com.candidate.management.dto.AlertDto;
import com.candidate.management.dto.DashboardStatsDto;
import com.candidate.management.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<AlertDto>> getActiveAlerts() {
        return ResponseEntity.ok(dashboardService.getActiveAlerts());
    }
}
