package com.anistebbal.starter.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateOrUpdateStreetDTO {

    @NotBlank(message = "Street name must not be blank")
    @Size(min = 3, message = "Street name must be at least 3 characters")
    private String name;

    @NotNull(message = "District ID is required")
    @Min(value = 1, message = "District ID must be a positive number")
    private Long districtId;

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getDistrictId() {
        return districtId;
    }

    public void setDistrictId(Long districtId) {
        this.districtId = districtId;
    }
}
