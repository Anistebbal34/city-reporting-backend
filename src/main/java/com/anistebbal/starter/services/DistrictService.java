package com.anistebbal.starter.services;

import com.anistebbal.starter.dto.DistrictResponseDTO;
import com.anistebbal.starter.dto.StreetResponseDTO;
import com.anistebbal.starter.entities.City;
import com.anistebbal.starter.entities.District;
import com.anistebbal.starter.repositories.CityRepository;
import com.anistebbal.starter.repositories.DistrictRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DistrictService {

        @Autowired
        private DistrictRepository districtRepository;

        @Autowired
        private CityRepository cityRepository;

        // Get all districts by city
        public List<DistrictResponseDTO> getDistrictDTOsByCity(Long cityId) {
                return districtRepository.findByCityId(cityId)
                                .stream()
                                .map(district -> {
                                        List<StreetResponseDTO> streetDTOs = district.getStreets()
                                                        .stream()
                                                        .map(street -> new StreetResponseDTO(
                                                                        street.getId(),
                                                                        street.getName(),
                                                                        district.getId(),
                                                                        district.getName()))
                                                        .collect(Collectors.toList());

                                        return new DistrictResponseDTO(
                                                        district.getId(),
                                                        district.getName(),
                                                        district.getCity().getId(),
                                                        district.getCity().getName(),
                                                        streetDTOs);
                                })
                                .collect(Collectors.toList());
        }

        public DistrictResponseDTO createDistrict(String name, Long cityId) {
                City city = cityRepository.findById(cityId)
                                .orElseThrow(() -> new EntityNotFoundException("City not found"));

                District district = new District();
                district.setName(name);
                district.setCity(city);
                District saved = districtRepository.save(district);

                return new DistrictResponseDTO(
                                saved.getId(),
                                saved.getName(),
                                city.getId(),
                                city.getName(),
                                List.of());
        }

        public DistrictResponseDTO updateDistrict(Long districtId, String newName, Long newCityId) {
                District district = districtRepository.findById(districtId)
                                .orElseThrow(() -> new EntityNotFoundException("District not found"));

                if (newName != null && !newName.trim().isEmpty()) {
                        district.setName(newName);
                }

                if (newCityId != null) {
                        City city = cityRepository.findById(newCityId)
                                        .orElseThrow(() -> new EntityNotFoundException("City not found"));
                        district.setCity(city);
                }

                District updated = districtRepository.save(district);

                List<StreetResponseDTO> streets = updated.getStreets()
                                .stream()
                                .map(street -> new StreetResponseDTO(
                                                street.getId(),
                                                street.getName(),
                                                updated.getId(),
                                                updated.getName()))
                                .toList();

                return new DistrictResponseDTO(
                                updated.getId(),
                                updated.getName(),
                                updated.getCity().getId(),
                                updated.getCity().getName(),
                                streets);
        }

        // Delete district — opti
        /*
         * public void deleteDistrict(Long districtId) {
         * if (!districtRepository.existsById(districtId)) {
         * throw new EntityNotFoundException("District not found");
         * }
         * districtRepository.deleteById(districtId);
         * }
         */
}
