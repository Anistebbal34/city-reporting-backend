package com.anistebbal.starter.dto;

import com.anistebbal.starter.entities.ReportStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateReportStatusDTO {

    @NotNull(message = "Status must not be null")
    private ReportStatus status;

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }
}
