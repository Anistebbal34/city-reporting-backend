package com.anistebbal.starter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistrictResponseDTO {
    private Long id;
    private String name;
    private Long cityId;
    private String cityName;
    private List<StreetResponseDTO> streets;
}
