package com.anistebbal.starter.services;

import com.anistebbal.starter.dto.AdminReportFilterDTO;
import com.anistebbal.starter.dto.CitizenReportFilterDTO;
import com.anistebbal.starter.dto.CitizenReportResponseDTO;
import com.anistebbal.starter.dto.CreateReportDTO;
import com.anistebbal.starter.dto.ReportAnalyticsDTO;
import com.anistebbal.starter.dto.ReportResponseCreationDTO;
import com.anistebbal.starter.dto.ReportResponseDTO;
import com.anistebbal.starter.entities.Report;
import com.anistebbal.starter.entities.ReportStatus;

import com.anistebbal.starter.entities.User;
import com.anistebbal.starter.repositories.ReportRepository;

import com.anistebbal.starter.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;

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

    // private static final Logger log =
    // LoggerFactory.getLogger(ReportController.class);

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

    private ReportResponseCreationDTO mapToCreationDto(Report report) {
        return ReportResponseCreationDTO.builder()
                .id(report.getId())
                .description(report.getDescription())
                .status(report.getStatus().name())
                .createdAt(report.getCreatedAt().toString())
                .streetName(report.getStreet().getName())
                .build();
    }

    private CitizenReportResponseDTO mapToCitizenDTO(Report report) {

        return CitizenReportResponseDTO.builder()
                .id(report.getId())
                .text(report.getDescription())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .build();
    }

    private ReportResponseDTO mapToAdminDTO(Report report) {
        return ReportResponseDTO.builder()
                .id(report.getId())
                .text(report.getDescription())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .streetName(report.getStreet().getName())
                .districtName(report.getStreet().getDistrict().getName())
                .build();
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
            String savedFilename = saveImageFile(imageFile);
            report.setImagePath(savedFilename);
        }

        Report saved = reportRepository.save(report);
        return mapToCreationDto(saved);
    }

    public List<CitizenReportResponseDTO> getReportsForCitizen(Long userId, CitizenReportFilterDTO filter) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Long streetId = user.getStreet().getId();

        LocalDate start = filter.getStartDate() != null ? filter.getStartDate() : LocalDate.now().minusDays(7);
        LocalDate end = filter.getEndDate() != null ? filter.getEndDate() : LocalDate.now();

        List<Report> reports = (filter.getStatus() != null)
                ? reportRepository.findByStreetIdAndStatusAndCreatedAtBetween(streetId, filter.getStatus(), start, end)
                : reportRepository.findByStreetIdAndCreatedAtBetween(streetId, start, end);

        return reports.stream().map(this::mapToCitizenDTO).toList();
    }

    public List<ReportResponseDTO> getReportsForAdmin(AdminReportFilterDTO filter) {
        if (filter.getDistrictId() == null) {
            throw new IllegalArgumentException("District ID is required.");
        }

        LocalDate start = filter.getStartDate() != null ? filter.getStartDate() : LocalDate.now().minusDays(7);
        LocalDate end = filter.getEndDate() != null ? filter.getEndDate() : LocalDate.now();

        return reportRepository.findAdminReportsWithFilter(
                filter.getDistrictId(),
                filter.getStreetId(),
                filter.getStatus(),
                start,
                end)
                .stream()
                .map(this::mapToAdminDTO)
                .toList();
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
            throw new SecurityException("Not authorized to access this report.");
        }
        return report;
    }

    public ReportResponseCreationDTO updateReportContent(Long userId, Long reportId, CreateReportDTO dto,
            MultipartFile newImage) throws IOException {
        Report report = getAuthorizedReport(userId, reportId);
        report.setDescription(dto.getText());

        if (newImage == null || newImage.isEmpty()) {
            deleteExistingImage(report);
            report.setImagePath(null);
        } else {
            deleteExistingImage(report);
            String newFileName = saveImageFile(newImage);
            report.setImagePath(newFileName);
        }

        Report saved = reportRepository.save(report);
        return mapToCreationDto(saved);
    }

    public void deleteReportByUser(Long userId, Long reportId) {
        Report report = getAuthorizedReport(userId, reportId);
        deleteExistingImage(report);
        reportRepository.delete(report);
    }

    private void deleteExistingImage(Report report) {
        if (report.getImagePath() != null) {
            File oldFile = new File(UPLOAD_DIR + report.getImagePath());
            if (oldFile.exists()) {
                oldFile.delete();
            }
        }
    }

}
