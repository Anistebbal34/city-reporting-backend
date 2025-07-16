package com.anistebbal.starter.services;

import com.anistebbal.starter.controllers.ReportController;
import com.anistebbal.starter.dto.AdminReportFilterDTO;
import com.anistebbal.starter.dto.CitizenReportFilterDTO;
import com.anistebbal.starter.dto.CitizenReportResponseDTO;
import com.anistebbal.starter.dto.CreateReportDTO;
import com.anistebbal.starter.dto.ReportAnalyticsDTO;
import com.anistebbal.starter.dto.ReportResponseCreationDTO;
import com.anistebbal.starter.dto.ReportResponseDTO;
import com.anistebbal.starter.entities.Report;
import com.anistebbal.starter.entities.ReportStatus;
import com.anistebbal.starter.entities.Street;
import com.anistebbal.starter.entities.User;
import com.anistebbal.starter.repositories.ReportRepository;
import com.anistebbal.starter.repositories.StreetRepository;
import com.anistebbal.starter.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class ReportService {

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/reports/";

    private static final Logger log = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    private boolean isImageProvided(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    private String saveImageFile(MultipartFile imageFile) throws IOException {
        System.out.println("🟡 Image received: " + imageFile.getOriginalFilename());
        System.out.println("🟡 Image size: " + imageFile.getSize());

        String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
        File destination = new File(UPLOAD_DIR + fileName);

        System.out.println("🛠️ Destination path: " + destination.getAbsolutePath());

        destination.getParentFile().mkdirs(); // ✅ This now works correctly

        try {
            imageFile.transferTo(destination);
            System.out.println("✅ Image saved successfully.");
        } catch (IOException e) {
            System.out.println("❌ Failed to save image: " + e.getMessage());
            throw e;
        }

        return fileName;
    }

    // CREATE REPORT
    public ReportResponseCreationDTO createReport(Long userId, String description, MultipartFile imageFile)
            throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Report report = Report.builder()
                .user(user)
                .street(user.getStreet())
                .description(description)
                .status(ReportStatus.PENDING)
                .build();

        if (isImageProvided(imageFile)) {
            System.out.println("📸 Image is provided, processing...");
            String savedFilename = saveImageFile(imageFile);
            report.setImagePath(savedFilename);
        } else {
            System.out.println("🚫 No image provided.");
        }

        Report savedReport = reportRepository.save(report);
        System.out.println(savedReport.getImagePath());
        return ReportResponseCreationDTO.builder()
                .id(report.getId())
                .description(savedReport.getDescription())
                .status(savedReport.getStatus().name())
                .createdAt(savedReport.getCreatedAt().toString())
                .streetName(savedReport.getStreet().getName())
                .build(); // ⬅Return a DTO directly
    }

    // FETCH for CITIZEN (by status, date, auto-street)
    public List<CitizenReportResponseDTO> getReportsForCitizen(Long userId, CitizenReportFilterDTO filter) {
        // 1. Validate user and get the street
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
        Long streetId = user.getStreet().getId();

        // 2. Handle default dates
        LocalDate startDate = filter.getStartDate() != null ? filter.getStartDate() : LocalDate.now().minusDays(7);
        LocalDate endDate = filter.getEndDate() != null ? filter.getEndDate() : LocalDate.now();
        log.info("📍 Street ID: {}, Start: {}, End: {}, Status: {}", streetId, startDate, endDate, filter.getStatus());

        // Fetch reports
        List<Report> reports;
        if (filter.getStatus() != null) {
            reports = reportRepository.findByStreetIdAndStatusAndCreatedAtBetween(
                    streetId,
                    filter.getStatus(),
                    startDate,
                    endDate);
        } else {
            reports = reportRepository.findByStreetIdAndCreatedAtBetween(
                    streetId,
                    startDate,
                    endDate);
        }
        log.info("📄 Reports fetched: {}", reports.size());

        return reports.stream().map(report -> {
            CitizenReportResponseDTO dto = new CitizenReportResponseDTO();
            dto.setId(report.getId());
            dto.setText(report.getDescription());
            dto.setStatus(report.getStatus());
            dto.setCreatedAt(report.getCreatedAt());
            return dto;
        }).toList();
    }

    public List<ReportResponseDTO> getReportsForAdmin(AdminReportFilterDTO filter) {
        if (filter.getDistrictId() == null) {
            throw new IllegalArgumentException("District ID is required for admin report filtering.");
        }

        LocalDate start = filter.getStartDate() != null
                ? filter.getStartDate()
                : LocalDate.now().minusDays(7);
        LocalDate end = filter.getEndDate() != null
                ? filter.getEndDate()
                : LocalDate.now();

        List<Report> reports = reportRepository.findAdminReportsWithFilter(
                filter.getDistrictId(),
                filter.getStreetId(),
                filter.getStatus(),
                start,
                end);

        return reports.stream().map(report -> {
            ReportResponseDTO dto = new ReportResponseDTO();
            dto.setId(report.getId());
            dto.setText(report.getDescription());
            dto.setStatus(report.getStatus());
            dto.setCreatedAt(report.getCreatedAt());
            dto.setStreetName(report.getStreet().getName());
            dto.setDistrictName(report.getStreet().getDistrict().getName());
            return dto;
        }).toList();
    }

    public Report updateStatus(Long reportId, ReportStatus newStatus) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("Report not found"));
        report.setStatus(newStatus);
        return reportRepository.save(report);
    }

    public void deleteReport(Long reportId) {
        if (!reportRepository.existsById(reportId)) {
            throw new EntityNotFoundException("Report not found");
        }
        reportRepository.deleteById(reportId);
    }

    public ReportAnalyticsDTO getReportAnalytics(LocalDate startDate, LocalDate endDate) {
        Object[] result = reportRepository.getReportAnalytics(startDate, endDate);

        int total = ((Number) result[0]).intValue();
        int resolved = ((Number) result[1]).intValue();
        int pending = ((Number) result[2]).intValue();

        double rate = total > 0 ? ((double) resolved / total) * 100 : 0.0;

        return ReportAnalyticsDTO.builder()
                .totalReports(total)
                .resolvedReports(resolved)
                .pendingReports(pending)
                .resolutionRate(rate)
                .build();
    }

    private Report getAuthorizedReport(Long userId, Long reportId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("Report not found"));

        boolean isOwner = report.getUser().getId().equals(userId);
        boolean sameStreet = report.getStreet().getId().equals(user.getStreet().getId());

        if (!isOwner && !sameStreet) {
            throw new SecurityException("You are not authorized to access this report");
        }

        return report;
    }

    public ReportResponseCreationDTO updateReportContent(Long userId, Long reportId, CreateReportDTO dto,
            MultipartFile newImage)
            throws IOException {

        Report report = getAuthorizedReport(userId, reportId);

        report.setDescription(dto.getText());

        if (newImage == null || newImage.isEmpty()) {

            if (report.getImagePath() != null) {
                File oldImage = new File(UPLOAD_DIR + report.getImagePath());
                if (oldImage.exists()) {
                    oldImage.delete();
                }
                report.setImagePath(null);
            }
        } else {
            // 🔁 Replace existing image with the new one
            if (report.getImagePath() != null) {
                File oldImage = new File(UPLOAD_DIR + report.getImagePath());
                if (oldImage.exists()) {
                    oldImage.delete();
                }
            }

            String fileName = UUID.randomUUID() + "_" + newImage.getOriginalFilename();
            File dest = new File(UPLOAD_DIR + fileName);
            dest.getParentFile().mkdirs();
            newImage.transferTo(dest);
            report.setImagePath(fileName);
        }

        Report saved = reportRepository.save(report);

        return ReportResponseCreationDTO.builder()
                .id(saved.getId())
                .description(saved.getDescription())
                .status(saved.getStatus().name())
                .createdAt(saved.getCreatedAt().toString())
                .streetName(saved.getStreet().getName())
                .build();
    }

    public void deleteReportByUser(Long userId, Long reportId) {
        Report report = getAuthorizedReport(userId, reportId);

        // Optional: delete image from disk if present
        if (report.getImagePath() != null) {
            File file = new File(UPLOAD_DIR + report.getImagePath());
            if (file.exists()) {
                file.delete();
            }
        }

        reportRepository.delete(report);
    }

}
