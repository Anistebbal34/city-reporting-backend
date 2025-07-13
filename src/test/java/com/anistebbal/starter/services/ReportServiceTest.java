package com.anistebbal.starter.services;

import com.anistebbal.starter.dto.CreateReportDTO;
import com.anistebbal.starter.dto.ReportResponseCreationDTO;
import com.anistebbal.starter.entities.Report;
import com.anistebbal.starter.entities.ReportStatus;
import com.anistebbal.starter.entities.Street;
import com.anistebbal.starter.entities.User;
import com.anistebbal.starter.repositories.ReportRepository;
import com.anistebbal.starter.repositories.StreetRepository;
import com.anistebbal.starter.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StreetRepository streetRepository;

    @InjectMocks
    private ReportService reportService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createReport_withImage_success() throws IOException {
        // Arrange
        Long userId = 1L;
        String description = "Broken light pole";
        MockMultipartFile imageFile = new MockMultipartFile("image", "photo.jpg", "image/jpeg",
                "image-data".getBytes());

        Street street = new Street();
        street.setId(1L);
        street.setName("Main Street");

        User user = new User();
        user.setId(userId);
        user.setStreet(street);

        Report savedReport = Report.builder()
                .id(123L)
                .description(description)
                .status(ReportStatus.PENDING)
                .createdAt(LocalDate.now())
                .street(street)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(reportRepository.save(any(Report.class))).thenReturn(savedReport);

        // Act
        ReportResponseCreationDTO response = reportService.createReport(userId, description, imageFile);

        // Assert
        assertNotNull(response);
        assertEquals(description, response.getDescription());
        assertEquals("PENDING", response.getStatus());
        assertEquals("Main Street", response.getStreetName());
    }

    @Test
    void createReport_userNotFound_throwsException() {
        // Arrange
        Long userId = 1L;
        String description = "Leaking pipe";
        MockMultipartFile imageFile = new MockMultipartFile("image", "photo.jpg", "image/jpeg",
                "image-data".getBytes());

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> reportService.createReport(userId, description, imageFile));
    }

    @Test
    void deleteReport_success() {
        Long reportId = 1L;
        when(reportRepository.existsById(reportId)).thenReturn(true);

        reportService.deleteReport(reportId);

        verify(reportRepository, times(1)).deleteById(reportId);
    }

    @Test
    void deleteReport_throwsIfNotExists() {
        Long reportId = 1L;
        when(reportRepository.existsById(reportId)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> reportService.deleteReport(reportId));
    }

    @Test
    void updateReportContent_success_withImageDeletion() throws IOException {
        Long userId = 1L;
        Long reportId = 2L;
        String oldImagePath = "old-image.jpg";

        User user = new User();
        user.setId(userId);
        Street street = new Street();
        street.setId(1L);
        user.setStreet(street);

        Report report = new Report();
        report.setId(reportId);
        report.setUser(user);
        report.setStreet(street);
        report.setImagePath(oldImagePath);
        report.setStatus(ReportStatus.PENDING);
        report.setCreatedAt(LocalDate.now());

        CreateReportDTO dto = new CreateReportDTO();
        dto.setText("Updated description");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(reportRepository.save(any(Report.class))).thenReturn(report);

        ReportResponseCreationDTO result = reportService.updateReportContent(userId, reportId, dto, null);

        assertEquals("Updated description", result.getDescription());
    }

    @Test
    void deleteReportByUser_success_withImage() {
        Long userId = 1L;
        Long reportId = 2L;

        User user = new User();
        user.setId(userId);
        Street street = new Street();
        street.setId(1L);
        user.setStreet(street);

        Report report = new Report();
        report.setId(reportId);
        report.setUser(user);
        report.setStreet(street);
        report.setImagePath("sample-image.jpg");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        reportService.deleteReportByUser(userId, reportId);

        verify(reportRepository).delete(report);
    }
}
