package com.anistebbal.starter.controllers;

import com.anistebbal.starter.dto.*;
import com.anistebbal.starter.entities.Report;

import com.anistebbal.starter.services.ReportService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.anistebbal.starter.config.UserPrincipal;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private static final Logger log = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private ReportService reportService;

    @PreAuthorize("hasRole('CITIZEN')")
    @PostMapping
    public ResponseEntity<ReportResponseCreationDTO> createReport(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestPart("data") CreateReportDTO dto,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {

        log.info("Received new report from user: {}", user.getUsername());
        log.info("DTO text: {}", dto.getText());
        log.info("Image present? {}", image);
        log.info("Image present? {}", image != null ? "Yes" : "No");

        ReportResponseCreationDTO responseDto = reportService.createReport(user.getId(), dto.getText(), image);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @GetMapping("/citizen")
    public ResponseEntity<List<CitizenReportResponseDTO>> getReportsForCitizen(
            @AuthenticationPrincipal UserPrincipal user,
            @ModelAttribute CitizenReportFilterDTO filter) {
        log.info("🔐 Accessed /citizen by user: {}", user.getUsername());
        log.info("📦 Filter received -> startDate: {}, endDate: {}, status: {}",
                filter.getStartDate(), filter.getEndDate(), filter.getStatus());

        Long userId = user.getId();
        List<CitizenReportResponseDTO> response = reportService.getReportsForCitizen(userId, filter);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReportResponseDTO>> getReportsForAdmin(
            @ModelAttribute AdminReportFilterDTO filter) {

        List<ReportResponseDTO> response = reportService.getReportsForAdmin(filter);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{reportId}/status")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<Report> updateStatus(
            @PathVariable Long reportId,
            @Valid @RequestBody UpdateReportStatusDTO dto) {
        Report updated = reportService.updateStatus(reportId, dto.getStatus());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{reportId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteReport(@PathVariable Long reportId) {
        reportService.deleteReport(reportId);
        return ResponseEntity.ok("Report deleted successfully");
    }

    @PreAuthorize("hasRole('ADMIN','CITIZEN')")

    @GetMapping("/analytics")
    public ResponseEntity<ReportAnalyticsDTO> getAnalytics(@Valid @RequestBody DateRangeDTO dateRange) {
        ReportAnalyticsDTO analytics = reportService.getReportAnalytics(
                dateRange.getStartDate(), dateRange.getEndDate());
        return ResponseEntity.ok(analytics);
    }

    @PutMapping("/{reportId}/user")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<ReportResponseCreationDTO> updateReportContent(
            @PathVariable Long reportId,
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestPart("data") CreateReportDTO dto,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {

        ReportResponseCreationDTO updated = reportService.updateReportContent(user.getId(), reportId, dto, image);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{reportId}/user")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<?> deleteReportByUser(
            @PathVariable Long reportId,
            @AuthenticationPrincipal UserPrincipal user) {
        reportService.deleteReportByUser(user.getId(), reportId);
        return ResponseEntity.ok("Report deleted successfully by user");
    }
}
