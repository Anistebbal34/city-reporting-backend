package com.anistebbal.starter.controllers;

import com.anistebbal.starter.dto.CreateOrUpdateDistrictDTO;
import com.anistebbal.starter.dto.CreateOrUpdateStreetDTO;
import com.anistebbal.starter.dto.DistrictResponseDTO;
import com.anistebbal.starter.dto.StreetResponseDTO;

import com.anistebbal.starter.services.DistrictService;
import com.anistebbal.starter.services.StreetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/location")
public class LocationController {

    @Autowired
    private DistrictService districtService;

    @Autowired
    private StreetService streetService;

    @GetMapping("/districts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DistrictResponseDTO>> getDistrictsByCity(@RequestParam Long cityId) {
        return ResponseEntity.ok(districtService.getDistrictDTOsByCity(cityId));
    }

    @PostMapping("/districts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DistrictResponseDTO> createDistrict(@Valid @RequestBody CreateOrUpdateDistrictDTO dto) {
        DistrictResponseDTO district = districtService.createDistrict(dto.getName(), dto.getCityId());
        return ResponseEntity.ok(district);
    }

    @PutMapping("/districts/{districtId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DistrictResponseDTO> updateDistrict(
            @PathVariable Long districtId,
            @Valid @RequestBody CreateOrUpdateDistrictDTO dto) {
        DistrictResponseDTO updated = districtService.updateDistrict(districtId, dto.getName(), dto.getCityId());
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/streets")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StreetResponseDTO>> getAllStreets() {
        return ResponseEntity.ok(streetService.getAllStreetDTOs());
    }

    @PostMapping("/streets")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StreetResponseDTO> createStreet(@Valid @RequestBody CreateOrUpdateStreetDTO dto) {
        StreetResponseDTO street = streetService.createStreet(dto.getName(), dto.getDistrictId());
        return ResponseEntity.ok(street);
    }

    // 🟡 Update an existing street
    @PutMapping("/streets/{streetId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StreetResponseDTO> updateStreet(
            @PathVariable Long streetId,
            @Valid @RequestBody CreateOrUpdateStreetDTO dto) {
        StreetResponseDTO updated = streetService.updateStreet(streetId, dto.getName(), dto.getDistrictId());
        return ResponseEntity.ok(updated);
    }
}
