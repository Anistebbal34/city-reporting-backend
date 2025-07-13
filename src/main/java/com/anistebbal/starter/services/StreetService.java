package com.anistebbal.starter.services;

import com.anistebbal.starter.dto.StreetResponseDTO;
import com.anistebbal.starter.entities.District;
import com.anistebbal.starter.entities.Street;
import com.anistebbal.starter.repositories.DistrictRepository;
import com.anistebbal.starter.repositories.StreetRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StreetService {

    @Autowired
    private StreetRepository streetRepository;

    @Autowired
    private DistrictRepository districtRepository;

    // Fetch all streets
    public List<StreetResponseDTO> getAllStreetDTOs() {
        return streetRepository.findAll()
                .stream()
                .map(street -> new StreetResponseDTO(
                        street.getId(),
                        street.getName(),
                        street.getDistrict().getId(),
                        street.getDistrict().getName()))
                .collect(Collectors.toList());
    }

    // Create new street
    public Street createStreet(String name, Long districtId) {
        District district = districtRepository.findById(districtId)
                .orElseThrow(() -> new EntityNotFoundException("District not found"));

        Street street = new Street();
        street.setName(name);
        street.setDistrict(district);
        return streetRepository.save(street);
    }

    // Update street
    public Street updateStreet(Long streetId, String newName, Long newDistrictId) {
        Street street = streetRepository.findById(streetId)
                .orElseThrow(() -> new EntityNotFoundException("Street not found"));

        if (newName != null && !newName.trim().isEmpty()) {
            street.setName(newName);
        }

        if (newDistrictId != null) {
            District newDistrict = districtRepository.findById(newDistrictId)
                    .orElseThrow(() -> new EntityNotFoundException("District not found"));
            street.setDistrict(newDistrict);
        }

        return streetRepository.save(street);
    }

    // Disable or remove street — optional
    // You can keep this commented until you implement soft-delete logic
    /*
     * public void deleteStreet(Long streetId) {
     * if (!streetRepository.existsById(streetId)) {
     * throw new EntityNotFoundException("Street not found");
     * }
     * streetRepository.deleteById(streetId);
     * }
     */
}
