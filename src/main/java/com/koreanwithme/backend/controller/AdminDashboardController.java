package com.koreanwithme.backend.controller;

import com.koreanwithme.backend.dto.AdminDashboardDtos.OverviewStatsResponse;
import com.koreanwithme.backend.service.AdminDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@CrossOrigin(origins = "*")
public class AdminDashboardController {

    @Autowired
    private AdminDashboardService adminDashboardService;

    @GetMapping("/overview")
    public ResponseEntity<OverviewStatsResponse> getOverviewStats() {
        return ResponseEntity.ok(adminDashboardService.getOverviewStats());
    }
}