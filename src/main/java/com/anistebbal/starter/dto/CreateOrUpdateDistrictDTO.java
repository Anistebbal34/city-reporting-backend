package com.anistebbal.starter.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateOrUpdateDistrictDTO {

    @NotBlank(message = "District name must not be blank")
    @Size(min = 3, message = "District name must be at least 3 characters")
    private String name;

    @NotNull(message = "City ID is required")
    @Min(value = 1, message = "City ID must be a positive number")
    private Long cityId;

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCityId() {
        return cityId;
    }

    public void setCityId(Long cityId) {
        this.cityId = cityId;
    }
}
