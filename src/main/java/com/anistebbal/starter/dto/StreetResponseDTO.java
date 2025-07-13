package com.anistebbal.starter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StreetResponseDTO {
    private Long id;
    private String name;
    private Long districtId;
    private String districtName;
}
