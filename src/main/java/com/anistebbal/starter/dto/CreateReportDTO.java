package com.anistebbal.starter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateReportDTO {

    @NotBlank(message = "Text is required")
    @Size(max = 200, message = "Text must not exceed 200 characters")
    private String text;

    // We will handle the image as MultipartFile in the controller
    // So no need to include it here unless you're using JSON upload

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
