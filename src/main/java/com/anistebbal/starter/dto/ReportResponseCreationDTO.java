package com.anistebbal.starter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ReportResponseCreationDTO {
    private Long id;
    private String description;
    private String imagePath;
    private String status;
    private String createdAt;
    private String streetName;
}
