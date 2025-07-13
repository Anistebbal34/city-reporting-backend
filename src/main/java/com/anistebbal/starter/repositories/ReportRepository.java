package com.anistebbal.starter.repositories;

import com.anistebbal.starter.entities.Report;
import com.anistebbal.starter.entities.ReportStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    // Existing methods
    List<Report> findByStreetId(Long streetId);

    List<Report> findByStatus(ReportStatus status);

    List<Report> findByCreatedAtBetween(LocalDate start, LocalDate end);

    List<Report> findByStreetDistrictId(Long districtId);

    List<Report> findByStreetDistrictCityId(Long cityId);

    List<Report> findByStreetIdAndStatus(Long streetId, ReportStatus status);

    List<Report> findByStreetIdAndCreatedAtBetween(Long streetId, LocalDate start, LocalDate end);

    List<Report> findByStreetIdAndStatusAndCreatedAtBetween(
            Long streetId, ReportStatus status, LocalDate start, LocalDate end);

    List<Report> findByStatusAndCreatedAtBetween(
            ReportStatus status, LocalDate start, LocalDate end);

    @Query("""
            SELECT r FROM Report r
            WHERE r.street.district.id = :districtId
              AND (:streetId IS NULL OR r.street.id = :streetId)
              AND (:status IS NULL OR r.status = :status)
              AND (r.createdAt BETWEEN :startDate AND :endDate)
            """)
    List<Report> findAdminReportsWithFilter(
            @Param("districtId") Long districtId,
            @Param("streetId") Long streetId,
            @Param("status") ReportStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query(value = """
            SELECT
                COUNT(*) AS total_reports,
                COUNT(*) FILTER (WHERE status = 'RESOLVED') AS resolved_reports,
                COUNT(*) FILTER (WHERE status = 'PENDING') AS pending_reports
            FROM report
            WHERE created_at BETWEEN :startDate AND :endDate
            """, nativeQuery = true)
    Object[] getReportAnalytics(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

}
