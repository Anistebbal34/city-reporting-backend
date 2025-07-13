package com.anistebbal.starter.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportAnalyticsDTO {
    private int totalReports;
    private int resolvedReports;
    private int pendingReports;
    private double resolutionRate; // (%) Example: 76.2
}
